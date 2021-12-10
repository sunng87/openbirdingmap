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
 ::localities
 (fn [db]
   (:localities db)))

(re-frame/reg-sub
 ::centroid
 (fn [db]
   (:centroid db)))

(re-frame/reg-sub
 ::current-state
 (fn [db] (:current-state db)))
