(ns  obmweb.views.map
  (:require
   [re-frame.core :as re-frame]
   [obmweb.events :as events]
   [obmweb.subs :as subs]
   ["react-leaflet" :as leaflet]
   ["leaflet" :as ll]))

(defn mapLocation []
  (let [map (leaflet/useMap)
        _ (leaflet/useMapEvent "locationfound" (fn [e] (.flyTo map (.-latlng e) (.getZoom map))))]
    (.locate map)
    nil))

(defn- centroid [localities]
  (if (not-empty localities)
    (let [c (count localities)
          centroid-lon (/ (apply + (mapv :lon localities)) c)
          centroid-lat (/ (apply + (mapv :lat localities)) c)]
      [centroid-lat centroid-lon])
    [0 0]))

(defn localityMarkers []
  (when-let [localities (not-empty @(re-frame/subscribe [::subs/localities]))]
    (map (fn [l]
            [:> leaflet/Marker
             {:position [(:lat l) (:lon l)]
              :icon (ll/Icon. #js{"iconUrl" "/images/bird.svg"
                                  "iconSize" (ll/Point. 32 32)})
              :key (:id l)}
             [:> leaflet/Popup {:key (:id l)} (:lname l)]])
         localities)))

(defn centerToLocalities []
  (when-let [localities (not-empty @(re-frame/subscribe [::subs/localities]))]
    (let [map (leaflet/useMap)
          center (clj->js (centroid localities))]
      (.flyTo map (ll/latLng. center) 10)
      nil)))

(defn setup-leaflet []
  [:> leaflet/MapContainer
   {:center [0 0]
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

   (localityMarkers)
   [:> centerToLocalities]
   ])
