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
    ". The source code, including data transformation tool, is open sourced on "
    [:a {:href "https://github.com/sunng87/openbirdingmap" :target "_blank"} "github"]
    "."]
   [:h3.bp-heading "Data Version"]
   [:ul.bp3-list
    [:li "CN-11: Jan 2021 - Dec 2021"]
    [:li "CN-32: Jan 2021 - Dec 2021"]]])

(defmethod routes/panels :about-panel [] [about-panel])
