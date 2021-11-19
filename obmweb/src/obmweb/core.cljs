(ns obmweb.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.views :as views]
   [obmweb.config :as config]

   ["react-leaflet" :as leaflet]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn setup-leaflet []
  [:> leaflet/MapContainer
   {:center [40.0822029 116.4624013]
    :zoom 13
    :attributionControl false}

   [:> leaflet/TileLayer
    {:attribution "Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"
     :url "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"}]

   [:> leaflet/AttributionControl
    {:position "bottomleft"
     }]])

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el))
  (let [map-el (.getElementById js/document "map")]
    (rdom/unmount-component-at-node map-el)
    (rdom/render [setup-leaflet] map-el)))

;; (defn setup-leaflet []
;;   (let [map (.map js/L "map")]
;;     (.setView map (clj->js [40.0822029 116.4624013]) 13)

;;     (let [layer (.tileLayer js/L "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
;;                             (clj->js {:attribution "Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"}))]
;;       (.addTo layer map))))


(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (setup-leaflet)
  (mount-root))
