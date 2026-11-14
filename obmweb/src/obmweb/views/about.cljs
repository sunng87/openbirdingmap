(ns obmweb.views.about
  (:require [re-frame.core :as rf]
            [clojure.string :as cstring]
            ["@astryxdesign/core/Heading" :as ax-heading]
            ["@astryxdesign/core/Skeleton" :as ax-skeleton]
            ["lucide-react" :as lucide]
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
     [:> ax-heading/Heading {:level 2} "About"]
     [:p
      "OpenBirdingMap is created by "
      [:a {:href "https://sunng.info" :target "_blank"} "Ning Sun"]
      ". This website displays latest birding records from "
      [:a {:href "https://ebird.org" :target "_blank"} "ebird"]
      ". The source code, including data transformation tool, is open sourced on "
      [:a {:href "https://github.com/sunng87/openbirdingmap" :target "_blank"} "github"]
      "."]
     [:> ax-heading/Heading {:level 3} "Data Version"]
     (if @metadata
       [:ul
        (for [m @metadata]
          [:li {:key (:id m)}
           (str (:id m) ": " (format-month-year (:date_start m)) " - " (format-month-year (:date_end m)))])]
       [:> ax-skeleton/Skeleton {:count 2}])]))

(defn footer-panel []
  [:footer.p2
   [:p
    [:a {:href "https://obm.sunng.info/"} "OpenBirdingMap"]
    " - Data from "
    [:a {:href "https://ebird.org" :target "_blank"} "eBird"]
    "."]
   [:p
    "Created with 💓, Clojure and ClojureScript. "
    [:a {:href "https://github.com/sunng87/openbirdingmap" :target "_blank"}
     [:> lucide/GitBranch {:size 14 :aria-hidden true}]]]])

(defmethod routes/panels :about-panel [] [about-panel])
