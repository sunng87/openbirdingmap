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

(defn navbar []
  (let [state (re-frame/subscribe [::subs/current-state])]
    [:div
   [:header
    [:> bp/Navbar
     [:> bp/NavbarGroup
      [:> bp/NavbarHeading "OpenBirdingMap"]
      [:> bp/NavbarDivider]
      [:> bp/AnchorButton {:minimal true
                           :icon "home"
                           :text "home"
                           :href (routes/url-for :home)}]
      [:> bp/AnchorButton {:minimal true
                           :icon "info-sign"
                           :text "about"
                           :href (routes/url-for :about)}]
      [:> bp/NavbarDivider]
      (state-select state)]]]]))
