(ns obmweb.views.about
  (:require [re-frame.core :as rf]
            ["@blueprintjs/core" :as bp]
            [obmweb.routes :as routes]
            [obmweb.views.nav :refer [navbar]]))

(defn about-panel []
  [:div.p2
   [:h2.bp-heading "About"]
   [:> bp/Text
    "OpenBirdingMap is created by "
    [:a {:href "https://sunng.info" :target "_blank"} "Ning Sun"]
    ". This website displays latest birding records from "
    [:a {:href "https://ebird.org" :target "_blank"} "ebird"]
    "."]])

(defmethod routes/panels :about-panel [] [about-panel])
