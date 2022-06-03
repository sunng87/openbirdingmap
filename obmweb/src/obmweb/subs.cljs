(ns obmweb.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::state
 (fn [db]
   (:state db)))

(re-frame/reg-sub
 ::map
 (fn [db]
   {:bounds (:bounds db)
    :localities (-> db :state :localities)}))

(re-frame/reg-sub
 ::current-state
 (fn [db] (:current-state db)))

(re-frame/reg-sub
 ::current-locality
 (fn [db] (:current-locality db)))

(re-frame/reg-sub
 ::current-species
 (fn [db]
   (select-keys db [:current-species
                    :current-species-media
                    :current-locality])))

(re-frame/reg-sub
 ::loading?
 (fn [db] (:loading? db)))
