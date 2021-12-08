(ns obmweb.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.views :as views]
   [obmweb.views.map :as map]
   [obmweb.config :as config]
))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el))
  (let [map-el (.getElementById js/document "map")]
    (rdom/unmount-component-at-node map-el)
    (rdom/render [map/setup-leaflet] map-el)))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::events/load-localities "CN-11"])
  (dev-setup)
  (mount-root))
