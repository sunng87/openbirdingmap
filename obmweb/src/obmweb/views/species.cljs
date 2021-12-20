(ns obmweb.views.species
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]

            [goog.string :as gstring]
            [goog.string.format]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(defn species-panel []
  (let [species-info (re-frame/subscribe [::subs/current-species])

        species (-> @species-info :current-species :species)
        records (-> @species-info :current-species :records)
        locality (-> @species-info :current-locality :locality)]
    [:<>
     [:div.p2
      [re-com/title
       :src (at)
       :label (:cname species)
       :level :level2]
      [re-com/label
       :label (:local_name species)]
      [re-com/label
       :label (:sname species)]
      [:p [:a {:target "_blank"
               :href (gstring/format "https://ebird.org/species/%s/%s"
                                     (:species_code species)
                                     (:state_code locality))}
           "View on ebird.org"]]]
     [:div.p2
      [re-com/title
       :src (at)
       :label "Observations"
       :level :level3]
      [re-com/label
       :label (:lname locality)]
      [:ul
       (map (fn [obs]
              [:li {:key (:id obs)}
               [:b.p1 (:record_count obs)]
               [:span.p1 (:record_date obs)]
               [:span.p1 (:observer_id obs)]])
            records)]]]))

(defmethod routes/panels :species-panel [] [species-panel])
