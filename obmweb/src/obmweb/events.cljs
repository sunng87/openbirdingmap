(ns obmweb.events
  (:require
   [re-frame.core :as re-frame]
   [obmweb.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [goog.string :as gstring]
   [goog.string.format]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-fx
  ::navigate
  (fn-traced [_ [_ handler]]
   {:navigate handler}))

(re-frame/reg-event-fx
 ::set-active-panel
 (fn-traced [{:keys [db]} [_ active-panel]]
            {:db (assoc db :active-panel active-panel)}))

(re-frame/reg-event-fx
 ::load-localities
 (fn-traced [{:keys [db]} [_ state-code]]
            {:http-xhrio {:method :get
                          :uri (gstring/format "http://localhost:8080/localities/%s" state-code)
                          :format (ajax/json-request-format)
                          :response-format (ajax/json-response-format {:keywords? true})
                          :on-success [::localities-loaded]
                          :on-failure [::localities-failed]}
             :db (assoc db :loading? true :current-state state-code :localities [])}))

(defn- centroid [localities]
  (if (not-empty localities)
    (let [c (count localities)
          centroid-lon (/ (apply + (mapv :lon localities)) c)
          centroid-lat (/ (apply + (mapv :lat localities)) c)]
      [centroid-lat centroid-lon])
    [0 0]))

(re-frame/reg-event-db
 ::localities-loaded
 (fn-traced [db [_ response]]
            (assoc db
                   :loading? false
                   :localities (:results response)
                   :centroid (centroid (:results response)))))

(re-frame/reg-event-db
 ::localities-failed
 (fn-traced [db _]
            (assoc db
                   :loading? false
                   :localities []
                   :centroid (centroid []))))

(re-frame/reg-event-fx
 ::request-locality
 (fn-traced [{:keys [db]} [_ locality-id]]
            {:http-xhrio {:method :get
                          :uri (gstring/format "http://localhost:8080/locality/%s" locality-id)
                          :format (ajax/json-request-format)
                          :response-format (ajax/json-response-format {:keywords? true})
                          :on-success [::locality-loaded]
                          :on-failure [::locality-failed]}
             :db (assoc db :loading? true :current-locality nil)}))

(re-frame/reg-event-db
 ::locality-loaded
 (fn-traced [db [_ response]]
            (assoc db
                   :loading? false
                   :current-locality (:results response)
                   :centroid (let [l (-> response :results :locality)]
                               [(:lat l) (:lon l)]))))

(re-frame/reg-event-db
 ::locality-failed
 (fn-traced [db _]
            (assoc db
                   :loading? false
                   :current-locality nil
                   :centroid (centroid []))))
