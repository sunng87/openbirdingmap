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
                   :key (:id l),
                   :on-click (fn [e]
                               (.stopPropagation (.-originalEvent e))
                               (re-frame/dispatch [::events/map-set-popup-info
                                                   l]))}
                  [:img
                   {:src "/images/bird.svg",
                    :weight 24,
                    :height 24,
                    :style {:cursor "pointer"}}]])
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
  [{bounds :bounds current :current}]
  (let [map (.-current (mapgl/useMap))]
    (when (not-empty bounds)
      (if (> (count bounds) 1)
        (.fitBounds map (clj->js (get-bounding-box bounds)))
        (do
          (.flyTo map (clj->js {:center (first bounds), :zoom 15}))
          (re-frame/dispatch [::events/map-set-popup-info current]))))
    nil))

(defn popup
  []
  (let [popup-info @(re-frame/subscribe [::subs/map-popup-info])]
    (when popup-info
      [:> mapgl/Popup
       {:anchor "top",
        :longitude (js/Number (:lon popup-info)),
        :latitude (js/Number (:lat popup-info)),
        :on-close #(re-frame/dispatch [::events/map-close-popup])}
       [:div
        [:a {:href (routes/url-for :locality :locality_id (:id popup-info))}
         (:lname popup-info)]]])))

(defn map-view
  []
  (let [map-data @(re-frame/subscribe [::subs/map])]
    [:> mapgl/Map
     {:initialViewState {:latitude 0, :longitude 0, :zoom 2},
      :mapStyle "https://tiles.openfreemap.org/styles/positron"}
     [:> mapgl/NavigationControl {:position "top-right"}]
     (localityMarkers (:localities map-data))
     [popup]
     [:f> centerToLocalities {:bounds (:bounds map-data)
                              :current (:current-locality map-data)}]]))
