(ns  obmweb.views.map
  (:require
   [re-frame.core :as re-frame]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.subs :as subs]
   ["react-leaflet" :as leaflet]
   ["leaflet" :as ll]))

(defn mapLocation []
  (let [map (leaflet/useMap)
        _ (leaflet/useMapEvent "locationfound" (fn [e] (.flyTo map (.-latlng e) (.getZoom map))))]
    (.locate map)
    nil))

(defn localityMarkers [localities]
  (when (not-empty localities)
    (map (fn [l]
            [:> leaflet/Marker
             {:position [(:lat l) (:lon l)]
              :icon (ll/Icon. #js{"iconUrl" "/images/bird.svg"
                                  "iconSize" (ll/Point. 32 32)})
              :key (:id l)}
             [:> leaflet/Popup {:key (:id l)}
              [:a {:href (routes/url-for :locality :id (:id l))} (:lname l)]]])
         localities)))

(defn centerToLocalities [{bounds :bounds}]
  (let [map (leaflet/useMap)
        bounds (clj->js bounds)]
    (when (not-empty bounds)
      (.flyToBounds map (ll/LatLngBounds. bounds)))
    nil))

(defn setup-leaflet []
  (let [map-data @(re-frame/subscribe [::subs/map])]
    [:> leaflet/MapContainer
     {:center [0 0]
      :zoom 2
      :attributionControl false
      :zoomControl false}

     [:> leaflet/TileLayer
      {:attribution "Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"
       :url "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"}]

     [:> leaflet/AttributionControl
      {:position "bottomright"}]
     [:> leaflet/ZoomControl
      {:position "topright"}]

     (localityMarkers (:localities map-data))
     [:f> centerToLocalities {:bounds (:bounds map-data)}]
     ]))
