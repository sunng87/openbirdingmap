(ns obmweb.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com :refer [at]]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.subs :as subs]))



;; home

(defn home-title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :src   (at)
     :label (str "Information")
     :level :level2]))

(defn home-panel []
  [re-com/v-box
   :src      (at)
   :gap      "1em"
   :children [[home-title]]])


(defmethod routes/panels :home-panel [] [home-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :src      (at)
     :height   "100%"
     :children [(routes/panels @active-panel)]]))
