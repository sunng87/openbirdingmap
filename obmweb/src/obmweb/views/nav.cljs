(ns obmweb.views.nav
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            ["@blueprintjs/core" :as bp]
            ["@blueprintjs/select" :as bsel]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(def supported-states
  [{:id "CN-11" :label "Beijing, China"}
   {:id "CN-32" :label "Jiangsu, China"}])

(defn state-select [state]
  (let [items supported-states
        initial-item (->> items (filter #(= (:id %) @state)) first)
        item-renderer (fn [item opt]
                        (reagent/as-element [:> bp/MenuItem {:key (.-id item)
                                                             :text (.-label item)
                                                             :onClick (.-handleClick opt)}]))]
    [:> bsel/Select {:items items
                     :activeItem initial-item
                     :itemRenderer item-renderer
                     :filterable false
                     :onItemSelect (fn [item]
                                     (.log js/console item)
                                     (re-frame/dispatch [::events/load-localities (.-id item)])
                                     (routes/navigate! :home))}
     [:> bp/Button {:text (:label initial-item)
                    :rightIcon :double-caret-vertical}]]))


(defn breadcrumbs [state panel locality species]
  (let [home {:text (->> supported-states (filter #(= (:id %) @state)) first :label)
              :href (routes/url-for :home)}
        items (case @panel
                :home-panel [(assoc home :current true)]
                :locality-panel (conj [home] (when-let [locality-info (:locality @locality)]
                                               {:text (:lname locality-info)
                                                :href (routes/url-for :locality :locality_id (:id locality-info))
                                                :current true}))
                :species-panel (conj [home]
                                     (when-let [locality (-> @species :current-locality :locality)]
                                       {:text (:lname locality)
                                        :href (routes/url-for :locality :locality_id (:id locality))})
                                     (when-let [species-info (-> @species :current-species :species)]
                                       {:text (-> species-info :cname)
                                        :href (routes/url-for :species
                                                              :locality_id (-> @species :current-locality :locality :id)
                                                              :species_id (-> species-info :id))
                                        :current true}))
                ;; not a information panel, :about for example
                nil)
        ;; remove nil items
        items (filter some? items)]
    (when (not-empty items)
      (let [item-renderer (fn [item]
                            (reagent/as-element [:> bp/Breadcrumb {:href (.-href item)
                                                                   :text (.-text item)
                                                                   :current (.-current item)}]))]
        [:div.p2.bp3-text-small [:> bp/Breadcrumbs {:items items
                                                    :breadcrumbRenderer item-renderer}]]))))

(defn navbar []
  (let [loading? (re-frame/subscribe [::subs/loading?])
        state (re-frame/subscribe [::subs/current-state])
        panel (re-frame/subscribe [::subs/active-panel])
        locality (re-frame/subscribe [::subs/current-locality])
        species (re-frame/subscribe [::subs/current-species])]
    [:div
     [:header
      [:> bp/Navbar
       [:> bp/NavbarGroup
        [:> bp/NavbarHeading "OpenBirdingMap"]
        [:> bp/NavbarDivider]
        (state-select state)]
       [:> bp/NavbarGroup {:align "right"}
        [:> bp/AnchorButton {:minimal true
                             :icon "home"
                             :text "home"
                             :href (routes/url-for :home)}]
        [:> bp/AnchorButton {:minimal true
                             :icon "info-sign"
                             :text "about"
                             :href (routes/url-for :about)}]]]]
     (when-not @loading? (breadcrumbs state panel locality species))]))
