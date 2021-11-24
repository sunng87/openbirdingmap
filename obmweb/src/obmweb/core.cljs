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

(defn mapLocation []
  (let [map (leaflet/useMap)
        _ (leaflet/useMapEvent "locationfound" (fn [e] (.flyTo map (.-latlng e) (.getZoom map))))]
    (.locate map)
    nil))

(defn setup-leaflet []
  [:> leaflet/MapContainer
   {:center [0,0]
    :zoom 14
    :attributionControl false
    :zoomControl false}

   [:> leaflet/TileLayer
    {:attribution "Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"
     :url "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"}]

   [:> leaflet/AttributionControl
    {:position "bottomright"}]
   [:> leaflet/ZoomControl
    {:position "topright"}]

   [:> mapLocation]])

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el))
  (let [map-el (.getElementById js/document "map")]
    (rdom/unmount-component-at-node map-el)
    (rdom/render [setup-leaflet] map-el)))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
