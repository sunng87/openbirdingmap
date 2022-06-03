(ns obmweb.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [obmweb.events :as events]
   [obmweb.routes :as routes]
   [obmweb.views :as views]

   [obmweb.config :as config]
))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::events/load-state "CN-11"])
  (dev-setup)
  (mount-root))
