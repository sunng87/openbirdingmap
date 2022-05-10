(ns obmweb.views.locality
  (:require [re-frame.core :as re-frame]
            ["@blueprintjs/core" :as bp]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(def month-selector-values
  (clj->js
   (let [labels ["All" "Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]
         values (concat [-1] (range 1 13))]
     (mapv #(hash-map :label %1 :value %2) labels values))))

(defn locality-panel []
  (let [locality-info (re-frame/subscribe [::subs/current-locality])
        locality (:locality @locality-info)
        species (:species @locality-info)]
    (if (some? locality)
      [:div.p2
       [:h2.bp3-heading (:lname locality)]
       [:div.mb2
        [:> bp/Icon {:icon "calendar" :className "mr1"}] "Filter by month:"
        [:> bp/HTMLSelect
         {:options month-selector-values
          :minimal true
          :className "ml1"
          :on-change #(let [value (.-value (.-target %))]
                        (if (= "-1" value)
                          ;; all
                          (re-frame/dispatch [::events/request-locality (:id locality)])
                          ;; by month
                          (re-frame/dispatch [::events/request-locality (:id locality) value])))}]]
       [:ul
        (map (fn [s]
               [:li {:key (:id s)}
                [:a {:href (routes/url-for :species :locality_id (:id locality) :species_id (:id s))}
                 (:local_name s)]
                [:span.ml1 (:cname s)]])
             species)]]

      [:div.p2
       [:> bp/Spinner]])))

(defmethod routes/panels :locality-panel [] [locality-panel])
