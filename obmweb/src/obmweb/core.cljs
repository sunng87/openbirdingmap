(ns obmweb.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.views :as views]
   [obmweb.config :as config]

   ["leaflet" :as leaflet]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn setup-leaflet []
  (let [map (.map js/L "map")]
    (.setView map (clj->js [40.0822029 116.4624013]) 13)

    (let [layer (.tileLayer js/L "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                            (clj->js {:attribution "Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"}))]
      (.addTo layer map))))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (setup-leaflet)
  (mount-root))
