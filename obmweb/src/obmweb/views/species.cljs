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
        media (-> @species-info :current-species-media)]
    (when species
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

       (if-let [image (:image media)]
         [:> bp/Card {:className "my1"}
          [:> bp/H3 "Image"]
          [:img.fit {:src (:src image) :alt (:alt image)}]
          [:span.bp3-ui-text (:alt image)]]
         [:> bp/Card {:className "bp3-skeleton"}
          [:> bp/H3 "Loading"]
          [:span.bp3-ui-text "text"]])

       (if-let [audios (not-empty (:recordings media))]
         [:> bp/Card {:className "my1"}
          [:> bp/H3 "Sounds"]
          (for [audio audios]
            [:div.mb1
             [:> bp/H4 (:rec audio)]
             [:p.bp3-ui-text (str (:date audio) ", " (:loc audio) ", " (:cnt audio) " | " (:length audio))]
             [:audio {:src (:file audio) :controls 1 :preload "none"}]
             [:img.fit {:src (-> audio :sono :full) :alt "sono"}]])
          ]
         [:> bp/Card {:className "bp3-skeleton"}
          [:> bp/H3 "Loading"]
          [:span.bp3-ui-text "text"]])

       [:> bp/Card {:className "my1"}
        [:> bp/H3 "Observations"]
        [:a {:href (routes/url-for :locality :locality_id (:id locality))}
         (:lname locality)]
        [:table.bp3-html-table.bp3-html-table-striped.bp3-html-table-bordered
         [:thead
          [:tr
           [:td "Date"]
           [:td "Amount"]
           [:td "Observer ID"]]]
         [:tbody
          (map (fn [obs]
                 [:tr {:key (:id obs)}
                  [:td (first (cstring/split (:record_date obs) #"T"))]
                  [:td [:b (:record_count obs)]]
                  [:td (:observer_id obs)]])
               records)]]]])))

(defmethod routes/panels :species-panel [] [species-panel])
