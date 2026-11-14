(ns obmweb.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   ["@astryxdesign/core/Heading" :as ax-heading]
   ["@astryxdesign/core/Skeleton" :as ax-skeleton]
   ["@astryxdesign/core/TabList" :as ax-tablist]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.subs :as subs]
   [obmweb.views.map :as map]
   [obmweb.views.nav :as nav]
   [obmweb.views.about :as views-about]
   [obmweb.views.locality :as views-locality]
   [obmweb.views.species]
   ))

;; home

(defn localities-list []
  (let [state (re-frame/subscribe [::subs/state])
        tab (r/atom "localities")]
    (fn []
      [:div.p2
       (if-let [l (-> @state :localities first)]
         [:> ax-heading/Heading {:level 2}
          (str (:state_name l) ", " (:country l))]
         [:> ax-skeleton/Skeleton {:width "60%" :height 24}])

       [:> ax-tablist/TabList
        {:aria-label "State data"
         :value @tab
         :onChange #(reset! tab %)}
        [:> ax-tablist/Tab {:value "localities" :label "Localities"}]
        [:> ax-tablist/Tab {:value "species" :label "Species"}]]

       (case @tab
         "localities"
         (when (not-empty (:localities @state))
           [:ul
            (for [l (:localities @state)]
              [:li {:key (str "locality-" (:id l))}
               [:a {:href (routes/url-for :locality :locality_id (:id l))}
                (:lname l)]
               [:span.chip (:species_count l) " species"]])])

         "species"
         (when (not-empty (:species @state))
           [:ul
            (for [s (:species @state)]
              [:li {:key (str "species-" (:id s))}
               [:a {:href "#"}
                (:local_name s)]
               [:span.ml1 (:cname s)]])])

         nil)])))

(defn home-panel []
  [:div
   [localities-list]])

(defmethod routes/panels :home-panel [] [home-panel])

;; main
(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:<>
     [nav/navbar]
     [:div#main
      [:div#map [map/map-view]]
      [:div#content
       [nav/breadcrumbs]
       (routes/panels @active-panel)
       [views-about/footer-panel]]]]))
