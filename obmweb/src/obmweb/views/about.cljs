(ns obmweb.views.about
  (:require [re-frame.core :as rf]
            [re-com.core :as re-com]
            [obmweb.routes :as routes]
            [obmweb.views.nav :refer [navbar]]))

(defn about-panel []
  [re-com/v-box
   :src (re-com/at)
   :children [[re-com/title :label "About" :level :level2]
              [re-com/p "OpenBirdingMap is created by "
               [re-com/hyperlink-href :href "https://sunng.info" :label "Ning Sun" :target "_blank"]
               ". This website displays latest birding records from "
               [re-com/hyperlink-href :href "https://ebird.org" :label "ebird" :target "_blank"]
               "."]]])

(defmethod routes/panels :about-panel [] [about-panel])
