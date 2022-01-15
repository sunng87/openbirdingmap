(ns obmweb.views
  (:require
   [re-frame.core :as re-frame]
   ["@blueprintjs/core" :as bp]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.subs :as subs]
   [obmweb.views.nav :refer [navbar]]
   [obmweb.views.about :as views-about]
   [obmweb.views.locality :as views-locality]
   [obmweb.views.species]
   ))

;; home

(defn localities-list []
  (let [localities (re-frame/subscribe [::subs/localities])]
    [:div.p2
     [:h2.bp3-heading
      (if-let [l (first @localities)]
        (str (:state_name l) ", " (:country l))
        "not loaded")]
     [:ul
      (map (fn [l]
             [:li {:key (:id l)}
              [:a {:href (routes/url-for :locality :id (:id l))}
               (:lname l)]])
           @localities)]]))

(defn home-panel []
  [:div
   [localities-list]])

(defmethod routes/panels :home-panel [] [home-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:div
     [navbar]
     (routes/panels @active-panel)]))
