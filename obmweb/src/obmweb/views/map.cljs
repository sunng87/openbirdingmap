(ns obmweb.views.map
  (:require [re-frame.core :as re-frame]
            [obmweb.events :as events]
            [obmweb.routes :as routes]
            [obmweb.subs :as subs]
            ["@blueprintjs/core" :as bp]
            ["react-map-gl/maplibre" :as mapgl]
            ["maplibre-gl" :as maplibre-gl]))

(defn localityMarkers
  [localities]
  (when (not-empty localities)
    (map (fn [l] [:> mapgl/Marker
                  {:latitude (:lat l),
                   :longitude (:lon l),
                   :anchor "bottom",
                   :key (:id l)}
                  [:img {:src "/images/bird.svg", :weight 16, :height 16}]
                  #_[:> leaflet/Popup {:key (:id l)}
                     [:a {:href (routes/url-for :locality :locality_id (:id l))}
                      [:> bp/Icon {:icon "map-marker", :className "mr1"}]
                      (:lname l)]]])
      localities)))

(defn- get-bounding-box
  [bounds]
  (doall (reduce (fn [[[sw-lon sw-lat] [ne-lon ne-lat]] [lon lat]]
                   ;; return in lnglat format
                   [[(min sw-lon lon) (min sw-lat lat)]
                    [(max ne-lon lon) (max ne-lat lat)]])
           [(first bounds) (first bounds)]
           bounds)))

(defn centerToLocalities
  [{bounds :bounds}]
  (let [map (.-current (mapgl/useMap))]
    (.log js/console (clj->js (get-bounding-box bounds)))
    (when (not-empty bounds)
      (if (> (count bounds) 1)
        (.fitBounds map (clj->js (get-bounding-box bounds)))
        (.flyTo map {:center (clj->js (first bounds)), :zoom 15})))
    nil))

(defn map-view
  []
  (let [map-data @(re-frame/subscribe [::subs/map])]
    [:> mapgl/Map
     {:initialViewState {:latitude 0, :longitude 0, :zoom 2},
      :mapStyle "https://tiles.openfreemap.org/styles/positron"}
     [:> mapgl/AttributionControl {:position "bottom-right"}]
     [:> mapgl/NavigationControl {:position "top-right"}]
     (localityMarkers (:localities map-data))
     [:f> centerToLocalities {:bounds (:bounds map-data)}]]))
