(ns obmweb.views.species
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            ["@blueprintjs/core" :as bp]
            [goog.string :as gstring]
            [goog.string.format]
            [clojure.string :as cstring]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(defn- audio-and-sono-view [audio]
  (let [sono-toggle (r/atom false)
        audio-icon (r/atom "play")
        player (atom nil)]
    (fn []
      [:<>
       [:div.flex.items-start
        [:div.mr2 [:> bp/Button {:icon @audio-icon :large true :outlined true
                                 :on-click #(if (.-paused @player)
                                              (.play @player)
                                              (.pause @player))}]]
        [:div
         [:> bp/H4 (:rec audio)]
         [:p.bp3-ui-text
          (str (:date audio) ", " (:loc audio) ", " (:cnt audio) " | " (:length audio)) " | "
          [:a {:href "#" :on-click #(swap! sono-toggle not)} "sono"]]
         [:audio.hide {:src (:file audio) :preload "none"
                       :ref #(reset! player %)
                       :on-play #(reset! audio-icon "pause")
                       :on-pause #(reset! audio-icon "play")}]
         [:> bp/Collapse {:isOpen @sono-toggle}
          [:img.fit.p1 {:src (-> audio :sono :full) :alt "sono"}]]]]])))

(defn species-panel []
  (let [species-info (re-frame/subscribe [::subs/current-species])

        species (-> @species-info :current-species :species)
        records (-> @species-info :current-species :records)
        locality (-> @species-info :current-locality :locality)
        other_localities (-> @species-info :current-species :other_localities)
        media (-> @species-info :current-species-media)]
    (when (and species locality)
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
          (doall
           (for [audio audios]
             [:div.mb2 {:key (:id audio)}
              [audio-and-sono-view audio]]))]
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
               records)]]]

       [:> bp/Card {:className "my1"}
        [:> bp/H3 "Also Seen at"]
        [:ul
         (map (fn [l]
           [:li {:key (:locality_id l)}
            [:a {:href (routes/url-for :species :locality_id (:locality_id l) :species_id (:id species))}
             (:lname l)]
            [:span.bp3-tag.bp3-round.bp3-minimal.ml1 (:c l) " times"]])
              other_localities)]]])))

(defmethod routes/panels :species-panel [] [species-panel])
