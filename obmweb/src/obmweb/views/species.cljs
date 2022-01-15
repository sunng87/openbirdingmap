(ns obmweb.views.species
  (:require [re-frame.core :as re-frame]
            ["@blueprintjs/core" :as bp]
            [goog.string :as gstring]
            [goog.string.format]
            [clojure.string :as cstring]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(defn species-panel []
  (let [species-info (re-frame/subscribe [::subs/current-species])

        species (-> @species-info :current-species :species)
        records (-> @species-info :current-species :records)
        locality (-> @species-info :current-locality :locality)
        image (-> @species-info :current-species-image)]
    (when species
      [:<>
       [:div.p2
        [:h2.bp3-heading  (:cname species)]
        [:p
         [:span.bp3-ui-text (:local_name species)]
         " | "
         [:span.bp3-ui-text (:sname species)]]
        [:p
         [:a {:href (gstring/format "https://ebird.org/species/%s/%s"
                                    (:species_code species)
                                    (:state_code locality))
              :target "_blank"}
          "ebird.org"]
         " | "
         [:a {:target "_blank"
              :href (gstring/format "https://en.wikipedia.org/wiki/%s"
                                    (cstring/replace (:cname species) #" " "_"))}
          "wikipedia.org"]
         " | "
         [:a {:target "_blank"
              :href (gstring/format "https://xeno-canto.org/explore?query=%s"
                                    (:cname species))}
          "xeno-canto.org"]]
        (when image
          [:<>
           [:img.fit {:src (:src image) :alt (:alt image)}]
           [:span.bp3-ui-text (:alt image)]])]
       [:div.p2
        [:h3.bp3-heading "Observations"]
        [:a {:href (gstring/format "/locality/%s" (:id locality))}
         (:lname locality)]
        [:ul
         (map (fn [obs]
                [:li {:key (:id obs)}
                 [:b.p1 (:record_count obs)]
                 [:span.p1 (first (cstring/split (:record_date obs) #"T"))]
                 [:span.p1 (:observer_id obs)]])
              records)]]])))

(defmethod routes/panels :species-panel [] [species-panel])
