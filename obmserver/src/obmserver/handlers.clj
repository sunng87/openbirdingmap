(ns obmserver.handlers
  (:require [obmserver.db :as db]
            [obmserver.crawling :as craw]
            [ring.util.response :as resp]))

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
    (resp/response {:results (-> (craw/to-ebird-url species-id)
                                 craw/fetch-html
                                 craw/parse-head-image)})))
