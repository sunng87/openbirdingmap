(ns obmweb.views.nav
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(defn navbar []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])
        state (re-frame/subscribe [::subs/current-state])]
    [:div
     [:header
      [re-com/h-box
       :src (at)
       :children [[re-com/title
                   :src (at)
                   :label "OpenBirdingMap"
                   :level :level1]
                  [re-com/single-dropdown
                   :choices [{:id "CN-11" :label "Beijing"}]
                   :on-change (fn [id] (re-frame/dispatch [::events/load-localities id]))
                   :model state]]]]
     [:nav.navbar.navbar-default
      [:div.container-fluid
       [:div.collapse.navbar-collapse
        [:ul.nav.navbar-nav
         (doall
          (for [item ["home" "about"]]
            (let [panel (str item "-panel")]
              [:li.nav-item {:class (when (= panel (name @active-panel)) "active")
                             :key (str "nav-" item)}
               [:a.nav-link {:on-click #(re-frame/dispatch [::events/navigate (keyword item)])
                             :href "#"}
                item]])))]]]]]))
