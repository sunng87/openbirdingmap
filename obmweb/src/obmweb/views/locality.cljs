(ns obmweb.views.locality
  (:require [re-frame.core :as re-frame]
            ["@blueprintjs/core" :as bp]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(defn locality-panel []
  (let [locality-info (re-frame/subscribe [::subs/current-locality])
        locality (:locality @locality-info)
        species (:species @locality-info)]
    [:div.p2
     [:h2.bp3-heading (:lname locality)]
     [:ul
      (map (fn [s]
             [:li {:key (:id s)}
              [:a {:href (routes/url-for :species :locality_id (:id locality) :species_id (:id s))}
               (:local_name s)]
              [:span.ml1 (:cname s)]])
           species)]]))

(defmethod routes/panels :locality-panel [] [locality-panel])
