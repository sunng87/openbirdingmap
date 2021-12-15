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
       :gap "2em"
       :children [[re-com/title
                   :src (at)
                   :label "OpenBirdingMap"
                   :level :level1]
                  [re-com/single-dropdown
                   :choices [{:id "CN-11" :label "Beijing, China"}]
                   :on-change (fn [id] (re-frame/dispatch [::events/load-localities id]))
                   :model state
                   :width "auto"]]]]
     [:nav.navbar.navbar-default
      [:div.container-fluid
       [:div.collapse.navbar-collapse
        [:ul.nav.navbar-nav
         (doall
          (for [item ["home" "about"]]
            (let [panel (str item "-panel")]
              [:li.nav-item {:class (when (and @active-panel (= panel (name @active-panel))) "active")
                             :key (str "nav-" item)}
               [:a.nav-link {:href (routes/url-for (keyword item))}
                item]])))]]]]]))
