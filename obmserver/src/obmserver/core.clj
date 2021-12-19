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

            [obmserver.handlers :as handlers]))

(defn wrap-cors2 [app]
  (wrap-cors app
             :access-control-allow-origin #".*"
             :access-control-allow-methods [:get]))

(def app
  (let [the-app (reitit/ring-handler
                 (reitit/router
                  [["/localities/:state_code" {:get handlers/list-localities}]
                   ["/locality/:locality_id" {:get handlers/list-species}]
                   ["/species/:species_id" {:get handlers/get-species}]
                   ["/species/:species_id/images/:state_id" {:get handlers/get-species-image}]])
                 (reitit/create-default-handler)
                 {:middleware [wrap-json-response
                               wrap-cors2
                               logger/wrap-with-logger]})]
    (wrap-defaults the-app api-defaults)))

(defn start-server []
  (jetty/run-jetty #'app {:port 8080 :join? false}))

(defstate ^:dynamic *webserver*
  :start (start-server)
  :stop (jetty/stop-server *webserver*))

(defn -main [& args]
  (mount/start))
