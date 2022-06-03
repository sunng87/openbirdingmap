(ns obmweb.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   ["@blueprintjs/core" :as bp]
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
  (let [state (re-frame/subscribe [::subs/state])]
    [:div.p2
     (if-let [l (-> @state :localities first)]
       [:h2.bp3-heading (str (:state_name l) ", " (:country l))]
       [:h2.bp3-heading.bp3-skeleton "not loaded"])
     [:> bp/Tabs {:id "state-tabs" :renderActiveTabPanelOnly true}

      [:> bp/Tab {:title "Localities"
                  :key "state-localities-tab"
                  :id "state-localities-tab"
                  :panel (r/as-element
                          (when (not-empty (:localities @state))
                            [:ul
                             (for [l (:localities @state)]
                               [:li {:key (str "locality-" (:id l))}
                                [:a {:href (routes/url-for :locality :locality_id (:id l))}
                                 (:lname l)]
                                [:span.bp3-tag.bp3-round.bp3-minimal.ml1 (:species_count l) " species"]])]))}]
      [:> bp/Tab {:title "Species"
                  :key "state-species-tab"
                  :id "state-species-tab"
                  :panel (r/as-element
                          (when (not-empty (:species @state))
                            [:ul
                             (for [s (:species @state)]
                               [:li {:key (str "species-" (:id s))}
                                [:a {:href "#"}
                                 (:local_name s)]
                                [:span.ml1 (:cname s)]])]))}]]]))

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
