(ns obmserver.handlers
  (:require [obmserver.db :as db]
            [ring.util.response :as resp]))

(defn list-localities [req]
  (let [state-code (-> req :path-params :state_code)
        localities (db/find-localities-by-state-id {:state_code state-code})]
    (resp/response {:results localities})))
