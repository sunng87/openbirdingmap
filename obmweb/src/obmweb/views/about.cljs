(ns obmweb.views.about
  (:require [re-frame.core :as rf]
            [clojure.string :as cstring]
            ["@blueprintjs/core" :as bp]
            [obmweb.routes :as routes]
            [obmweb.subs :as subs]
            [obmweb.views.nav :refer [navbar]]))

(def ^:private month-names
  ["Jan" "Feb" "Mar" "Apr" "May" "Jun"
   "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"])

(defn- format-month-year
  "Format an ISO date like 2020-01-21 as Jan 2020"
  [iso-date]
  (let [[y m] (cstring/split iso-date #"-")]
    (str (nth month-names (dec (js/parseInt m 10))) " " y)))

(defn about-panel []
  (let [metadata (rf/subscribe [::subs/metadata])]
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
     (if @metadata
       [:ul.bp5-list
        (for [m @metadata]
          [:li {:key (:id m)}
           (str (:id m) ": " (format-month-year (:date_start m)) " - " (format-month-year (:date_end m)))])]
       [:ul.bp5-list
        [:li.bp5-skeleton "loading"]])]))

(defn footer-panel []
  [:footer.p2
   [:p.bp5-ui-text
    [:a {:href "https://obm.sunng.info/"} "OpenBirdingMap"]
    " - Data from "
    [:a {:href "https://ebird.org" :target "_blank"} "eBird"]
    "."]
   [:p.bp5-ui-text
    "Created with 💓, Clojure and ClojureScript. "
    [:a {:href "https://github.com/sunng87/openbirdingmap" :target "_blank"}
     [:> bp/Icon {:icon "git-repo"}]]]])

(defmethod routes/panels :about-panel [] [about-panel])
