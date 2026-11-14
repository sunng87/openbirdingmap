(ns obmweb.views.locality
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            ["@astryxdesign/core/Icon" :as ax-icon]
            ["@astryxdesign/core/DropdownMenu" :as ax-dropdown]
            ["@astryxdesign/core/Spinner" :as ax-spinner]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(def ^:private month-selector-items
  (let [labels ["All" "Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]
        values (concat [-1] (range 1 13))]
    (mapv #(hash-map :label %1 :value %2) labels values)))

(defn- month-menu-items [locality]
  (clj->js (mapv (fn [{:keys [label value]}]
                   {:id (str value)
                    :label label
                    :onClick (fn []
                               (re-frame/dispatch
                                (if (= -1 value)
                                  ;; all
                                  [::events/request-locality (:id locality)]
                                  ;; by month
                                  [::events/request-locality (:id locality) (str value)])))})
                 month-selector-items)))

(defn locality-panel []
  (let [locality-info (re-frame/subscribe [::subs/current-locality])
        selected-month (r/atom -1)
        locality (:locality @locality-info)
        species (:species @locality-info)]
    (if (some? locality)
      [:div.p2
       [:h2 (:lname locality)]
       [:div.mb2
        [:> ax-icon/Icon {:icon "calendar"}] " Filter by month: "
        [:span.ml1
         [:> ax-dropdown/DropdownMenu
          {:items (month-menu-items locality)
           :button {:label (->> month-selector-items
                                (filter #(= (:value %) @selected-month))
                                first
                                :label)
                    :variant "ghost"
                    :endContent (r/as-element [:> ax-icon/Icon {:icon "chevronDown"}])}}]]]
       [:ul
        (map (fn [s]
               [:li {:key (:id s)}
                [:a {:href (routes/url-for :species :locality_id (:id locality) :species_id (:id s))}
                 (:local_name s)]
                [:span.ml1 (:cname s)]])
             species)]]

      [:div.p2
       [:> ax-spinner/Spinner]])))

(defmethod routes/panels :locality-panel [] [locality-panel])
