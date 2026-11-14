(ns obmweb.views.species
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            ["chart.js" :as chart]
            ["react-chartjs-2" :as chartcomps]
            ["lucide-react" :as lucide]
            ["@astryxdesign/core/Button" :as ax-button]
            ["@astryxdesign/core/Card" :as ax-card]
            ["@astryxdesign/core/Collapsible" :as ax-collapsible]
            ["@astryxdesign/core/Heading" :as ax-heading]
            ["@astryxdesign/core/Lightbox" :as ax-lightbox]
            ["@astryxdesign/core/Skeleton" :as ax-skeleton]
            ["@astryxdesign/core/Spinner" :as ax-spinner]
            [goog.string :as gstring]
            [goog.string.format]
            [clojure.string :as cstring]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

;; register scales
(.register chart/Chart
           chart/CategoryScale
           chart/LinearScale
           chart/Title
           chart/BarElement
           chart/Tooltip
           chart/Legend)

(defn- audio-and-sono-view [audio]
  (let [playing? (r/atom false)
        player (atom nil)]
    (fn []
      (let [has-audio? (not-empty (:file audio))]
        [:<>
         [:div.flex.items-start
          [:div.mr2
           [:> ax-button/Button
            {:label (if @playing? "pause audio" "play audio")
             :isIconOnly true
             :variant "secondary"
             :isDisabled (not has-audio?)
             :icon (r/as-element
                    (if @playing?
                      [:> lucide/Pause {:size 18 :aria-hidden true}]
                      [:> lucide/Play {:size 18 :aria-hidden true}]))
             :on-click #(if (.-paused @player)
                          (do
                            ;; pause other audios
                            (doseq [a (.querySelectorAll js/document "audio")]
                              (.pause a))
                            (.play @player))
                          (.pause @player))}]]
          [:div
           [:> ax-heading/Heading {:level 4} (:type audio)]
           [:p
            [:span.mr2 [:> lucide/User {:size 14 :aria-hidden true}] (:rec audio)]
            [:span.mr2 [:> lucide/Calendar {:size 14 :aria-hidden true}] (:date audio)]
            [:span.mr2 [:> lucide/Clock {:size 14 :aria-hidden true}] (:length audio)]
            [:br]
            [:span.mr2 [:> lucide/MapPin {:size 14 :aria-hidden true}] (str (:loc audio) ", " (:cnt audio))]]
           [:audio.hide {:src (:file audio) :preload "none"
                         :ref #(reset! player %)
                         :on-play #(reset! playing? true)
                         :on-pause #(reset! playing? false)}]]]
         [:> ax-collapsible/Collapsible
          {:trigger "spectrogram"
           :defaultIsOpen false}
          [:div.overflow-auto.mt1
           [:img.pr1.pl1 {:src (-> audio :sono :full) :alt "sono"}]]]]))))

(defn- photo-credits
  "Credit bar for one photo; used both inline and as the Lightbox caption."
  [image n i]
  [:div.photo-label-bar
   [:div
    (when (not-empty (:title image))
      [:div.photo-label-title (:title image)])
    [:div.photo-label-attr
     "© "
     [:b (:author image)]
     " "
     (:state image) ", " (:country image)
     " | "
     [:a {:target "_blank" :href (:link image)}
      (:citation image)]]]
   [:span.photo-label-counter (str (inc i) " / " n)]])

(defn- image-gallery [images]
  (let [idx (r/atom 0)
        ;; srcs that have finished loading; any other image shows a skeleton
        loaded-srcs (r/atom #{})
        open? (r/atom false)
        open! #(reset! open? true)]
    (fn [images]
      (let [n (count images)
            i (-> @idx (min (dec n)) (max 0))
            image (nth images i nil)
            loaded? (contains? @loaded-srcs (:src image))]
        (when image
          [:div
           ;; inline frame: current photo + credits; opens the gallery
           [:div.photo-frame {:role "button"
                              :tab-index 0
                              :aria-label "Open photo gallery"
                              :on-click open!
                              :on-key-down #(when (contains? #{"Enter" " "} (.-key %))
                                              (.preventDefault %)
                                              (open!))}
            (when-not loaded?
              [:> ax-skeleton/Skeleton {:className "photo-skeleton"}])
            [:img.fit {:src (:src image)
                       :alt (:alt image)
                       :style (when-not loaded? {:display "none"})
                       :on-load #(swap! loaded-srcs conj (:src image))}]
            [photo-credits image n i]]
           ;; astryx Lightbox provides prev/next, arrow keys and swipe
           [:> ax-lightbox/Lightbox
            {:aria-label "Species photos"
             :isOpen @open?
             :onOpenChange #(reset! open? %)
             :index i
             :onIndexChange #(reset! idx %)
             :media (clj->js
                     (map-indexed (fn [j img]
                                    {:src (:src img)
                                     :alt (:alt img)
                                     :caption (r/as-element [photo-credits img n j])})
                                  images))}]])))))


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

(defn- section
  "Collapsible titled section, replacing blueprint Section + SectionCard."
  [title & children]
  [:> ax-collapsible/Collapsible {:trigger title :defaultIsOpen true}
   (into [:> ax-card/Card] children)])

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
       [:> ax-heading/Heading {:level 2} (:cname species)]
       [:p
        [:span (:local_name species)]
        " | "
        [:span (:sname species)]]
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

       [section "Photos"
        (cond
          ;; media still loading
          (nil? media)
          [:> ax-skeleton/Skeleton {:height 200}]

          (empty? (:images media))
          [:p "No photos available."]

          :else
          [image-gallery (:images media)])]

       [section "Sounds"
        (cond
          (nil? media)
          [:> ax-skeleton/Skeleton {:height 200}]

          (empty? (:recordings media))
          [:p "No recordings available."]

          :else
          (doall
           (for [audio (:recordings media)]
             ^{:key (:id audio)}
             [audio-and-sono-view audio])))]

       (when (and (not-empty records) (some? locality))
         [section "Observations"
          [:p
           [:a {:href (routes/url-for :locality :locality_id (:id locality))}
            (:lname locality)]]
          [:table.data-table
           [:thead
            [:tr
             [:th "Date"]
             [:th "Amount"]
             [:th "Observer ID"]]]
           [:tbody
            (map (fn [obs]
                   [:tr {:key (:id obs)}
                    [:td (first (cstring/split (:record_date obs) #"T"))]
                    [:td [:b (:record_count obs)]]
                    [:td (:observer_id obs)]])
                 records)]]])

       [section "Also Seen at"
        [:ul
         (map (fn [l]
                [:li {:key (:locality_id l)}
                 [:a {:href (routes/url-for :species :locality_id (:locality_id l) :species_id (:id species))}
                  (:lname l)]
                 [:span.chip (:c l) " times"]])
              other-localities)]]

       [section "Weekly Record Stats"
        [:div
         [chart-view weekly-stats]]]]

      ;; loading
      [:div.p2
       [:> ax-spinner/Spinner]])))

(defmethod routes/panels :species-panel [] [species-panel])
