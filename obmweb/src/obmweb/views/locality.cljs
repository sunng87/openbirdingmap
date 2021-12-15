(ns obmweb.views.locality
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(defn locality-panel []
  (let [locality-info (re-frame/subscribe [::subs/current-locality])
        locality (:locality @locality-info)
        species (:species @locality-info)]
    [:div.p2
     [re-com/title
      :src (at)
      :label (:lname locality)
      :level :level2]
     [:ul
      (map (fn [s]
             [:li {:key (:id s)}
              [:a {:href (routes/url-for :species :id (:id s))
                   :on-click (fn [_]
                               ;; TODO
                               )}
               (:cname s)]])
           species)]]))

(defn locality-parent-panel []
  [re-com/v-box
   :src (re-com/at)
   :children [[locality-panel]]])

(defmethod routes/panels :locality-panel [] [locality-parent-panel])
