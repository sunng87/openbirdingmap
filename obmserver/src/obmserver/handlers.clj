(ns obmserver.handlers
  (:require [obmserver.db :as db]
            [obmserver.crawling :as craw]
            [ring.util.response :as resp]
            [manifold.deferred :as m]
            [manifold.executor :as exec]))

(defn list-localities [req]
  (let [state-code (-> req :path-params :state_code)
        localities (db/find-localities-by-state-id {:state_code state-code})]
    (resp/response {:results localities})))

(defn list-species [req]
  (let [locality-id (-> req :path-params :locality_id)
        locality (db/find-locality-by-id {:id locality-id})
        species-ids (db/find-species-by-locality-id {:locality_id locality-id})
        species (db/find-species-by-ids {:ids (mapv :species_id species-ids)})]
    (resp/response {:results {:locality locality
                              :species species}})))

(defn get-species [req]
  (let [species-id (-> req :path-params :species_id)
        locality-id (-> req :params :locality_id)
        ;; query
        species (db/find-species-by-id {:id species-id})
        records (when locality-id
                  (db/find-records-by-species-and-locality {:species_id species-id
                                                            :locality_id locality-id}))]
    (resp/response {:results {:species species
                              :records records}})))

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
