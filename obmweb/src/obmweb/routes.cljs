(ns obmweb.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as re-frame]
   [obmweb.events :as events]))

(defmulti panels identity)
(defmethod panels :default [] [:div "No panel found for this route."])

(def routes
  (atom
    ["/" {""      :home
          "about" :about
          "locality/" {[:id] :locality}
          "species/" {[:id] :species}}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn dispatch
  [route]
  (let [handler-name (:handler route)
        panel (keyword (str (name handler-name) "-panel"))]
    (re-frame/dispatch [::events/set-active-panel panel])

    ;; additional url based data loading
    (condp = handler-name
      :home (re-frame/dispatch [::events/reset-bound])
      :locality (re-frame/dispatch [::events/request-locality (-> route :route-params :id)])
      nil)))

(defonce history
  (pushy/pushy dispatch parse))

(defn navigate!
  [handler]
  (pushy/set-token! history (url-for handler)))

(defn start!
  []
  (pushy/start! history))

(re-frame/reg-fx
  :navigate
  (fn [handler]
    (navigate! handler)))
