(ns obmweb.views.nav
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            ["@astryxdesign/core/Icon" :as ax-icon]
            ["@astryxdesign/core/Breadcrumbs" :as ax-breadcrumbs]
            ["@astryxdesign/core/DropdownMenu" :as ax-dropdown]
            ["@astryxdesign/core/TopNav" :as ax-topnav]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(defn state-select [state]
  (let [metadata @(re-frame/subscribe [::subs/metadata])
        items (mapv #(hash-map :id (:id %) :label (:lname %)) metadata)
        initial-item (->> items (filter #(= (:id %) @state)) first)
        menu-items (clj->js (mapv (fn [item]
                                    {:id (:id item)
                                     :label (:label item)
                                     :onClick (fn []
                                                (re-frame/dispatch [::events/load-state (:id item)])
                                                (routes/navigate! :home))})
                                  items))]
    (when (some? initial-item)
      [:> ax-dropdown/DropdownMenu
       {:items menu-items
        :button {:label (:label initial-item)
                 :variant "ghost"
                 :endContent (r/as-element [:> ax-icon/Icon {:icon "chevronDown"}])}}])))

(defn breadcrumbs [state panel locality species]
  (when-not @(re-frame/subscribe [::subs/loading?])
    (let [state (re-frame/subscribe [::subs/current-state])
          panel (re-frame/subscribe [::subs/active-panel])
          locality (re-frame/subscribe [::subs/current-locality])
          species (re-frame/subscribe [::subs/current-species])
          home {:text (->> @(re-frame/subscribe [::subs/metadata])
                           (filter #(= (:id %) @state))
                           first
                           :lname)
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
                                       ;; double check species-info
                                       ;; and locality
                                       (when-let [species-info (-> @species :current-species :species)]
                                         (when-let [locality (-> @species :current-locality :locality)]
                                           {:text (-> species-info :cname)
                                            :href (routes/url-for :species
                                                                  :locality_id (-> @species :current-locality :locality :id)
                                                                  :species_id (-> species-info :id))
                                            :current true})))
                  ;; not a information panel, :about for example
                  nil)
          ;; remove nil items
          items (filter some? items)]
      (when (not-empty items)
        [:div.p2
         (into [:> ax-breadcrumbs/Breadcrumbs
                {:aria-label "Breadcrumb"
                 :variant "supporting"}]
               (map (fn [item]
                      [:> ax-breadcrumbs/BreadcrumbItem
                       (cond-> {:key (:text item)}
                         (not (:current item)) (assoc :href (:href item)))
                       (:text item)])
                    items))]))))

(defn navbar []
  (let [state (re-frame/subscribe [::subs/current-state])]
    [:header
     [:> ax-topnav/TopNav
      {:heading (r/as-element
                 [:> ax-topnav/TopNavHeading
                  {:heading "OpenBirdingMap"
                   :headingHref (routes/url-for :home)}])
       :startContent (r/as-element [state-select state])
       :endContent (r/as-element
                    [:div.nav-end
                     [:> ax-topnav/TopNavItem {:href (routes/url-for :home)}
                      "home"]
                     [:> ax-topnav/TopNavItem {:href (routes/url-for :about)}
                      "about"]])}]]))
