(ns obmweb.core
  (:require
   [reagent.dom.client :as rdom-client]
   [re-frame.core :as re-frame]
   ["@astryxdesign/core/theme" :as ax-theme]
   ["@astryxdesign/theme-neutral/built" :as neutral-theme]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.views :as views]

   [obmweb.config :as config]
))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

;; react 19: mount via createRoot (ReactDOM.render was removed)
(defonce react-root
  (rdom-client/create-root (.getElementById js/document "app")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rdom-client/render react-root
                      [:> ax-theme/Theme {:theme neutral-theme/neutralTheme}
                       [views/main-panel]]))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::events/load-metadata])
  (dev-setup)
  (mount-root))
