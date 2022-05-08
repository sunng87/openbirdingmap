(ns obmserver.handlers
  (:require [obmserver.db :as db]
            [obmserver.crawling :as craw]
            [ring.util.response :as resp]
            [manifold.deferred :as m]
            [manifold.executor :as exec]))

(defn list-localities [req]
  (let [state-code (-> req :path-params :state_code)
        localities (db/find-localities-by-state-id {:state_code state-code})
        locality-species-stats (db/stat-species-by-localities {:locality_ids (mapv :id localities)})
        locality-species-stats-map (into {} (map #(vector (:locality_id %) (:c %)) locality-species-stats))
        localities (->> localities
                        (mapv #(assoc % :species_count (get locality-species-stats-map (:id %) 0)))
                        (sort-by :species_count >))]
    (resp/response {:results localities})))

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
        locality-id (-> req :params :locality_id)
        ;; query
        species (db/find-species-by-id {:id species-id})
        records (db/find-records-by-species-and-locality {:species_id species-id
                                                          :locality_id locality-id})

        current-state-code (:state_code (db/find-locality-by-id {:id locality-id}))
        other-localities-and-count (db/find-localities-records-by-species {:species_id species-id
                                                                           :state_code current-state-code})
        species-weekly-raw-stats (db/stat-species-records-by-week {:species_id species-id
                                                                   :state_code current-state-code})
        weekly-stats-array (let [weeks (int-array 53 0)]
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

(defn get-species-media [req]
  (let [species-id (-> req :path-params :species_id)
        species (db/find-species-by-id {:id species-id})
        species-code (:species_code species)
        sci-name (:sname species)

        image (m/future-with pool (craw/images species-code))
        recordings (m/future-with pool (craw/recordings sci-name))]
    (resp/response {:results {:image @image
                              :recordings @recordings}})))
