(ns obmserver.handlers
  (:require [obmserver.db :as db]
            [obmserver.crawling :as craw]
            [clj-http.client :as http]
            [ring.util.response :as resp]
            [manifold.deferred :as m]
            [manifold.executor :as exec]))

(defn get-metadata [req]
  (-> {:results (db/find-metadata)}
      (resp/response)
      (assoc-in [:headers "Cache-Control"] "public,max-age=3600,s-maxage=3600")))

(defn list-localities [req]
  (let [state-code (-> req :path-params :state_code)
        localities (db/find-localities-by-state-id {:state_code state-code})
        locality-species-stats (db/stat-species-by-localities {:locality_ids (mapv :id localities)})
        locality-species-stats-map (into {} (map #(vector (:locality_id %) (:c %)) locality-species-stats))
        localities (->> localities
                        (mapv #(assoc % :species_count (get locality-species-stats-map (:id %) 0)))
                        (sort-by :species_count >))]
    (resp/response {:results localities})))

(defn get-state-info [req]
  (let [state-code (-> req :path-params :state_code)
        localities (db/find-localities-by-state-id {:state_code state-code})
        locality-species-stats (db/stat-species-by-localities {:locality_ids (mapv :id localities)})
        locality-species-stats-map (into {} (map #(vector (:locality_id %) (:c %)) locality-species-stats))
        localities (->> localities
                        (mapv #(assoc % :species_count (get locality-species-stats-map (:id %) 0)))
                        (sort-by :species_count >))

        species-ids (db/find-species-by-state-id {:state_code state-code})
        species (db/find-species-by-ids {:ids (mapv :species_id species-ids)})]
    (resp/response {:results {:localities localities
                              :species species}})))

(defn list-species [req]
  (let [locality-id (-> req :path-params :locality_id)
        month (-> req :params :month)

        locality (db/find-locality-by-id {:id locality-id})
        species-ids (if (nil? month)
                      (db/find-species-by-locality-id {:locality_id locality-id})
                      (db/find-species-by-locality-id-and-month {:locality_id locality-id
                                                                 :month month}))
        species (if (not-empty species-ids)
                  (db/find-species-by-ids {:ids (mapv :species_id species-ids)})
                  [])]
    (resp/response {:results {:locality locality
                              :species species}})))

(defn get-species [req]
  (let [species-id (-> req :path-params :species_id)
        ;; optional
        locality-id (-> req :params :locality_id)
        state-code (-> req :params :state_code)
        ;; query
        species (db/find-species-by-id {:id species-id})
        records (when locality-id
                  (db/find-records-by-species-and-locality {:species_id species-id
                                                            :locality_id locality-id}))

        current-state-code (or state-code
                               (:state_code (db/find-locality-by-id {:id locality-id})))
        other-localities-and-count (db/find-localities-records-by-species {:species_id species-id
                                                                           :state_code current-state-code})
        species-weekly-raw-stats (db/stat-species-records-by-week {:species_id species-id
                                                                   :state_code current-state-code})
        weekly-stats-array (let [weeks (int-array 54 0)]
                             (doseq [{w :w c :c} species-weekly-raw-stats]
                               (aset weeks w c))
                             (seq weeks))]
    (resp/response {:results {:species species
                              :records records
                              :other_localities other-localities-and-count
                              :weekly_stats weekly-stats-array}})))

;; deprecated
(defn get-species-image [req]
  (let [species-id (-> req :path-params :species_id)]
    (resp/response {:results {:image (craw/images species-id)}})))

(def pool (exec/fixed-thread-executor 10))

;; server-side cache for crawled media: upstream sites (birdsoftheworld.org,
;; xeno-canto) take 2-14s per call, way beyond any db query; media for a
;; species changes rarely, so cache aggressively.
(defonce ^:private media-cache (atom {}))

(def ^:private media-cache-ttl-ms (* 24 3600 1000))
;; cache failures briefly only, they are usually transient (upstream slowness)
(def ^:private media-cache-failure-ttl-ms (* 5 60 1000))

(defn- cached-species-media [species-id]
  (let [now (System/currentTimeMillis)
        entry (get @media-cache species-id)]
    (if (and entry (< (- now (:fetched-at entry)) (:ttl entry)))
      (:data entry)
      (let [species (db/find-species-by-id {:id species-id})
            species-code (:species_code species)
            sci-name (:sname species)
            images (m/future-with pool (craw/images species-code))
            recordings (m/future-with pool (craw/recordings sci-name))
            data {:images @images
                  :recordings @recordings}
            failed? (and (empty? (:images data)) (empty? (:recordings data)))]
        (swap! media-cache assoc species-id
               {:fetched-at now
                :ttl (if failed? media-cache-failure-ttl-ms media-cache-ttl-ms)
                :data data})
        data))))

;; --- media proxy -----------------------------------------------------------
;; Media lives on external CDNs (Macaulay photos on
;; cdn.download.ams.birds.cornell.edu, spectrograms/audio on xeno-canto.org).
;; Serving it through the API keeps browser/CDN hotlink rules out of the
;; picture and lets us normalize asset URLs.

(def ^:private media-host-allowlist
  #{"cdn.download.ams.birds.cornell.edu"
    "birdsoftheworld.org"
    "xeno-canto.org"
    "www.xeno-canto.org"})

(defn- allowed-media-url?
  [u]
  (try
    (let [host (.getHost (java.net.URI. ^String u))]
      (boolean (some #(or (= host %) (.endsWith ^String host (str "." %)))
                     media-host-allowlist)))
    (catch Exception _ false)))

(defn- normalize-asset-url
  "Macaulay asset URLs need an explicit size segment; data-asset-src comes
  without one and the CDN answers 404 NoSuchKey."
  [u]
  (if (re-find #"/api/v[12]/asset/\d+$" u)
    (str u "/1200")
    u))

(defn media-proxy [req]
  (let [u (-> req :params :url)]
    (if-not (and (string? u) (allowed-media-url? u))
      {:status 403 :body "media host not allowed"}
      (try
        (let [r (http/get (normalize-asset-url u)
                          {:socket-timeout 10000
                           :connection-timeout 5000
                           :as :byte-array})]
          (if (= 200 (:status r))
            (-> (resp/response (:body r))
                (resp/content-type (get-in r [:headers "Content-Type"] "application/octet-stream"))
                (assoc-in [:headers "Cache-Control"] "public,max-age=86400"))
            {:status 502 :body (str "upstream status " (:status r))}))
        (catch Exception e
          {:status 502 :body (str "upstream error: " (.getMessage e))})))))

(defn- proxied-media-url
  [u]
  (when u
    (str "/api/media?url="
         (java.net.URLEncoder/encode ^String u "UTF-8"))))

(defn- rewrite-media-urls
  "Point browser-facing media at the proxy; :link stays direct (it is a page
  URL, not media)."
  [media]
  (-> media
      (update :images (fn [images]
                        (mapv #(update % :src proxied-media-url) images)))
      (update :recordings (fn [recordings]
                            (mapv (fn [rec]
                                    (-> rec
                                        (update :file proxied-media-url)
                                        (update :sono (fn [s]
                                                        (some-> s (update :full proxied-media-url))))))
                                  recordings)))))

(defn get-species-media [req]
  (let [species-id (-> req :path-params :species_id)]
    (->  {:results (rewrite-media-urls (cached-species-media species-id))}
         (resp/response)
         (assoc-in [:headers "Cache-Control"] "public,max-age=3600,s-maxage=3600"))))
