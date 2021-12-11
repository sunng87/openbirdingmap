(ns obmserver.handlers
  (:require [obmserver.db :as db]
            [ring.util.response :as resp]))

(defn list-localities [req]
  (let [state-code (-> req :path-params :state_code)
        localities (db/find-localities-by-state-id {:state_code state-code})]
    (resp/response {:results localities})))

(defn list-species [req]
  (let [locality-id (-> req :path-params :locality_id)
        species-ids (db/find-species-by-locality-id {:locality_id locality-id})
        species (db/find-species-by-ids {:ids (mapv :species_id species-ids)})]
    (resp/response {:results species})))
