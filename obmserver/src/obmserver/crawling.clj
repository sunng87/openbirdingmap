(ns obmserver.crawling
  (:require [stavka.core :as sta]
            [clj-http.client :as http])
  (:import [org.jsoup Jsoup]))

(defn to-bow-url [species-id]
  (format "https://birdsoftheworld.org/bow/species/%s/cur/introduction" species-id))

(defn fetch-html [url]
  (try
    (let [r (http/get url {:socket-timeout 5000
                           :connection-timeout 3000})]
      (when (= (:status r) 200)
        (:body r)))
    (catch Exception _ nil)))

(defn parse-head-image [html]
  (let [soup (Jsoup/parse ^String html)]
    {:src (some-> (.select soup "meta[property=og:image]") first (.attr "content"))
     :alt (some-> (.select soup "meta[property=og:image:alt]") first (.attr "content"))}))

(defn parse-body-images [html]
  (let [soup (Jsoup/parse ^String html)
        items (.select soup "a[data-media-type=photo]")]
    (mapv #(hash-map :src (.attr % "data-asset-src")
                     :alt (.attr % "data-asset-comname")
                     :title (.attr % "data-asset-title")
                     :author (.attr % "data-asset-username")
                     :citation (.attr % "data-asset-citationname")
                     :state (.attr % "data-asset-subnational1")
                     :country (.attr % "data-asset-country")
                     :link (.attr % "data-asset-mllink"))
          items)))

(defn- fetch-bow-html
  "Fetch the BOW introduction page for a species. BOW slugs do not always
  match eBird species codes (e.g. whoswa1 -> whoswa, redcro1 -> redcro), so
  when the direct URL fails, retry with the trailing digit stripped."
  [species-id]
  (or (fetch-html (to-bow-url species-id))
      (when-let [[_ stripped] (re-matches #"^(.*?)(\d+$)" species-id)]
        (fetch-html (to-bow-url stripped)))))

(defn images [species-id]
  (if-let [html (fetch-bow-html species-id)]
    (parse-body-images html)
    []))

(def ^:private xeno-canto-api-url "https://xeno-canto.org/api/3/recordings")

(defn- fetch-xeno-canto [sname api-key]
  ;; API v3 requires tag-based queries, so search by the species tag
  (try
    (let [r (http/get xeno-canto-api-url
                      {:socket-timeout 5000
                       :connection-timeout 3000
                       :as :json
                       :query-params {"query" (format "sp:\"%s\"" sname)
                                      "key" api-key}})]
      (when (= (:status r) 200)
        (:body r)))
    (catch Exception _ nil)))

(defn parse-recordings [data]
  (when-let [recordings (not-empty (:recordings data))]
    (take 5 (map #(select-keys % [:file :cnt :rec :loc :length :date :type :sono :id]) recordings))))

(defn recordings
  ([species-name] (recordings species-name (sta/$$ :xeno-canto-api-key)))
  ([species-name api-key]
   (when (and (not-empty species-name) (not-empty api-key))
     (-> (fetch-xeno-canto species-name api-key)
         parse-recordings))))
