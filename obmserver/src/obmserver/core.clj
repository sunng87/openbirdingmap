(ns obmserver.core
  (:gen-class)
  (:require [ring.adapter.jetty9 :as jetty]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :as resp]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.logger :as logger]
            [reitit.ring :as reitit]
            [mount.core :as mount :refer [defstate]]
            [stavka.core :as sta]

            [obmserver.handlers :as handlers]))

(defn wrap-cors2 [app]
  (wrap-cors app
             :access-control-allow-origin #".*"
             :access-control-allow-methods [:get]))

(def app
  (let [the-app (reitit/ring-handler
                 (reitit/router
                  ["/api"
                   ["/state/:state_code" {:get handlers/get-state-info}]
                   ["/localities/:state_code" {:get handlers/list-localities}] ;; deprecated
                   ["/locality/:locality_id" {:get handlers/list-species}]
                   ["/species/:species_id" {:get handlers/get-species}]
                   ["/species/:species_id/image" {:get handlers/get-species-image}] ;; deprecated
                   ["/species/:species_id/media" {:get handlers/get-species-media}]])
                 (reitit/create-default-handler)
                 {:middleware [wrap-json-response
                               wrap-cors2
                               logger/wrap-with-logger]})]
    (wrap-defaults the-app api-defaults)))

(defn start-server []
  (jetty/run-jetty #'app {:port (sta/$$l :http-port 8080) :join? false}))

(defstate ^:dynamic *webserver*
  :start (start-server)
  :stop (jetty/stop-server *webserver*))

(defn setup-stavka! []
  (sta/global! (sta/env)
               (sta/edn (sta/file "./config.edn"))))

(defn start []
  (setup-stavka!)
  (mount/start))

(defn -main [& args]
  (start))
