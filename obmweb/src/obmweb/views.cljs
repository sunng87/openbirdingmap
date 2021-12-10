(ns obmweb.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com :refer [at]]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.subs :as subs]
   [obmweb.views.nav :refer [navbar]]
   [obmweb.views.about :as views-about]))


;; home

(defn home-title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :src   (at)
     :label (str "Information")
     :level :level2]))

(defn localities-list []
  (let [localities (re-frame/subscribe [::subs/localities])]
    [:div.p2
     [re-com/title
      :src (at)
      :label (if-let [l (first @localities)]
               (str (:state_name l) ", " (:country l))
               "not loaded")
      :level :level2]
     [:ul
      (map (fn [l]
             [:li {:key (:id l)}
              [:a {:href "#"
                   :on-click #(re-frame/dispatch [::events/navigate :localities (:id l)])}
               (:lname l)]])
           @localities)]]))

(defn home-panel []
  [re-com/v-box
   :src      (at)
   :children [[navbar] [localities-list]]])

(defmethod routes/panels :home-panel [] [home-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :src      (at)
     :height   "100%"
     :children [(routes/panels @active-panel)]]))
