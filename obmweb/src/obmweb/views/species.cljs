(ns obmweb.views.species
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            ["chart.js" :as chart]
            ["@blueprintjs/core" :as bp]
            ["react-chartjs-2" :as chartcomps]
            [goog.string :as gstring]
            [goog.string.format]
            [clojure.string :as cstring]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(def ^:const icon-play "play")
(def ^:const icon-pause "pause")
(def ^:const icon-disable "disable")

;; register scales
(.register chart/Chart
           chart/CategoryScale
           chart/LinearScale
           chart/Title
           chart/BarElement
           chart/Tooltip
           chart/Legend)

(defn- audio-and-sono-view [audio]
  (let [sono-toggle (r/atom false)
        audio-icon (r/atom (if (not-empty (:file audio))
                             icon-play
                             icon-disable))
        player (atom nil)]
    (fn []
      [:<>
       [:div.flex.items-start
        [:div.mr2 [:> bp/Button {:icon @audio-icon :large true :outlined true
                                 :disabled (empty? (:file audio))
                                 :on-click #(if (.-paused @player)
                                              (do
                                                ;; pause other audios
                                                (doseq [a (.querySelectorAll js/document "audio")]
                                                  (.pause a))
                                                (.play @player))
                                              (.pause @player))}]]
        [:div
         [:> bp/H4 (:type audio)]
         [:p.bp5-running-text
          [:span.mr2 [:> bp/Icon {:icon "record"} :className "mr1"] (:rec audio)]
          [:span.mr2 [:> bp/Icon {:icon "calendar" :className "mr1"}] (:date audio)]
          [:span.mr2 [:> bp/Icon {:icon "time" :className "mr1"}] (:length audio)]
          [:br]
          [:span.mr2 [:> bp/Icon {:icon "map-marker" :className "mr1"}] (str (:loc audio) ", " (:cnt audio))]
          [:span.mr2 [:> bp/Icon {:icon "timeline-bar-chart" :className "mr1"}]
           [:a {:href "#" :on-click #(swap! sono-toggle not)} "sono"]]]
         [:audio.hide {:src (:file audio) :preload "none"
                       :ref #(reset! player %)
                       :on-play #(reset! audio-icon icon-pause)
                       :on-pause #(reset! audio-icon icon-play)}]]]
       [:> bp/Collapse {:isOpen @sono-toggle}
        [:div.overflow-auto.mt1
         [:img.pr1.pl1 {:src (-> audio :sono :full) :alt "sono"}]]]])))

(defn- current-week []
  (let [today (js/Date.)
        start-of-year (js/Date. (.getFullYear today) 0 1)
        days-since (Math/floor (/ (- today start-of-year) (* 24 60 60 1000)))]
    (Math/ceil (/ days-since 7))))

(defn- chart-view [weekly-stats]
  (let [bg-color (update (into [] (repeat 53 "#D3D8DE")) (current-week) (constantly "#738091"))]
    [:> chartcomps/Bar {:data {:labels (mapv #(str "W" %) (range 0 54))
                            :datasets [{:data weekly-stats
                                        :label "Weekly record stats"
                                        :backgroundColor bg-color}]}}]))

(defn species-panel []
  (let [species-info (re-frame/subscribe [::subs/current-species])

        species (-> @species-info :current-species :species)

        ;; only available when accesing /localities/:locality-id/species/:species-id
        records (-> @species-info :current-species :records)
        locality (-> @species-info :current-locality :locality)

        other-localities (-> @species-info :current-species :other_localities)
        weekly-stats (-> @species-info :current-species :weekly_stats)
        media (-> @species-info :current-species-media)]
    (if species
      [:div.p2
       [:h2.bp5-heading  (:cname species)]
       [:p
        [:span.bp5-ui-text (:local_name species)]
        " | "
        [:span.bp5-ui-text (:sname species)]]
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

       [:> bp/Section {:title "Images"
                       :collapsible true}
        (if-let [images (not-empty (:images media))]
         [:> bp/SectionCard
          [:> bp/Tabs {:id "image-tabs" :renderActiveTabPanelOnly true}
           (doall
            (for [image (map-indexed #(assoc %2 :idx %1) images)]
              [:> bp/Tab {:title (or (not-empty (:title image)) (:idx image))
                          :key (:idx image)
                          :id (str "image-tab-" (:idx image))
                          :panel (r/as-element [:<>
                                                [:img.fit {:src (:src image) :alt (:alt image)}]
                                                [:p.bp5-ui-text (:alt image)]])}]))]]
         [:> bp/SectionCard {:className "bp5-skeleton"}])]


       [:> bp/Section {:title "Sounds"
                       :collapsible true}
        (if-let [audios (not-empty (:recordings media))]
          (doall
           (for [audio audios]
             [:> bp/SectionCard {:key (:id audio)}
              [audio-and-sono-view audio]]))
          [:> bp/SectionCard {:className "bp5-skeleton"}])]


       (when (and (not-empty records) (some? locality))
         [:> bp/Section {:title "Observations"
                         :collapsible true}
          [:> bp/SectionCard
           [:a {:href (routes/url-for :locality :locality_id (:id locality))}
            (:lname locality)]
           [:table.bp5-html-table.bp5-html-table-striped.bp5-html-table-bordered
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
                  records)]]]])

       [:> bp/Section {:title "Also Seen at"
                       :collapsible true}
        [:> bp/SectionCard
         [:ul
          (map (fn [l]
                 [:li {:key (:locality_id l)}
                  [:a {:href (routes/url-for :species :locality_id (:locality_id l) :species_id (:id species))}
                   (:lname l)]
                  [:span.bp5-tag.bp5-round.bp5-minimal.ml1 (:c l) " times"]])
               other-localities)]]]

       [:> bp/Section {:title "Weekly Record Stats"
                       :collapsible true}
        [:> bp/SectionCard
         [:div
          [chart-view weekly-stats]]]]]

      ;; loading
      [:div.p2
       [:> bp/Spinner]])))

(defmethod routes/panels :species-panel [] [species-panel])
