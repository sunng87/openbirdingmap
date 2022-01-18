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

(def url-root "http://localhost:8080/api")
(defn url [path & vars]
  (apply gstring/format (str url-root path) vars))

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
 (fn-traced [{:keys [db]} [_ active-panel route]]
            (let [new-db (assoc db :active-panel active-panel)
                  handler-name (:handler route)]
              (condp = handler-name
                ;; additional panel based events to trigger
                :home {:db new-db :dispatch [::reset-bound]}
                :locality {:db new-db :dispatch [::request-locality (-> route :route-params :locality_id)]}
                ;; load locality if current-locality is empty
                :species (let [locality-id (-> route :route-params :locality_id)
                               fx [[:dispatch [::request-species
                                               locality-id
                                               (-> route :route-params :species_id)]]]
                               fx (if-not (:current-locality db)
                                    (conj fx [:dispatch [::request-locality locality-id]])
                                    fx)]
                           {:db new-db :fx fx})
                {:db new-db}))))

(re-frame/reg-event-fx
 ::load-localities
 (fn-traced [{:keys [db]} [_ state-code]]
            {:http-xhrio {:method :get
                          :uri (url "/localities/%s" state-code)
                          :format (ajax/json-request-format)
                          :response-format (ajax/json-response-format {:keywords? true})
                          :on-success [::localities-loaded]
                          :on-failure [::localities-failed [::load-localities]]}
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
                   :bounds (mapv #(vector (:lat %) (:lon %)) (:results response)))))

(re-frame/reg-event-db
 ::localities-failed
 (fn-traced [db _]
            (assoc db
                   :loading? false
                   :localities []
                   :bounds nil)))

(re-frame/reg-event-fx
 ::request-locality
 (fn-traced [{:keys [db]} [_ locality-id]]
            {:http-xhrio {:method :get
                          :uri (url "/locality/%s" locality-id)
                          :format (ajax/json-request-format)
                          :response-format (ajax/json-response-format {:keywords? true})
                          :on-success [::locality-loaded]
                          :on-failure [::request-failed]}
             :db (assoc db :loading? true :current-locality nil)}))

(re-frame/reg-event-db
 ::locality-loaded
 (fn-traced [db [_ response]]
            (assoc db
                   :loading? false
                   :current-locality (:results response)
                   :bounds (let [l (-> response :results :locality)]
                             [[(:lat l) (:lon l)]]))))

(re-frame/reg-event-db
 ::request-failed
 (fn-traced [db _]
            (assoc db :loading? false)))

(re-frame/reg-event-db
 ::reset-bound
 (fn-traced [db _]
            (assoc db
                   :bounds (mapv #(vector (:lat %) (:lon %)) (:localities db)))))

(re-frame/reg-event-fx
 ::request-species
 (fn-traced [{:keys [db]} [_ locality-id species-id]]
            (let [endpoint (url "/species/%s?locality_id=%s" species-id locality-id)]
              {:http-xhrio {:method :get
                            :uri endpoint
                            :format (ajax/json-request-format)
                            :response-format (ajax/json-response-format {:keywords? true})
                            :on-success [::species-loaded]
                            :on-failure [::request-failed [::request-species]]}
               :db (assoc db :loading? true :current-species nil :current-species-image nil)})))

(re-frame/reg-event-fx
 ::species-loaded
 (fn-traced [{:keys [db]} [_ response]]
            (let [results (:results response)
                  current-locality (:current-locality db)]
              {:db (assoc db :loading? false :current-species results)
               :dispatch [::request-species-image [(-> results :species :species_code)]]})))

(re-frame/reg-event-db
 ::species-failed
 (fn-traced [db _]
            (assoc db :loading? false)))

(re-frame/reg-event-fx
 ::request-species-image
 (fn-traced [{:keys [db]} [_ [species-id]]]
            (let [endpoint(url "/species/%s/image" species-id)]
              {:http-xhrio {:method :get
                            :uri endpoint
                            :format (ajax/json-request-format)
                            :response-format (ajax/json-response-format {:keywords? true})
                            :on-success [::species-image-loaded]
                            :on-failure [::request-failed [::request-sepcies-image]]} ;; TODO
               :db (assoc db :current-species-image nil)})))

(re-frame/reg-event-db
 ::species-image-loaded
 (fn-traced [db [_ response]]
            (assoc db :current-species-image (-> response :results :image))))
