(ns obmweb.views.nav
  (:require [re-frame.core :as re-frame]
            [obmweb.events :as events]
            [obmweb.subs :as subs]
            [obmweb.routes :as routes]))

(defn navbar []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:nav.navbar.navbar-default
     [:div.container-fluid
      [:div.collapse.navbar-collapse
       [:ul.nav.navbar-nav
        (for [item ["home" "about"]]
          (let [panel (str item "-panel")]
            [:li.nav-item {:class (when (= panel (name @active-panel)) "active")}
             [:a.nav-link {:on-click #(re-frame/dispatch [::events/navigate (keyword item)])
                           :href "#"}
              item]]))]]]]))
