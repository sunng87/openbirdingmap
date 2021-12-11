(ns obmserver.core
  (:gen-class)
  (:require [ring.adapter.jetty9 :as jetty]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :as resp]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [ring.middleware.cors :refer [wrap-cors]]
            [reitit.ring :as reitit]
            [mount.core :as mount :refer [defstate]]

            [obmserver.handlers :as handlers]))

(defn the-handler [req]
  (resp/response {:data "ok"}))

(defn wrap-cors2 [app]
  (wrap-cors app
             :access-control-allow-origin #".*"
             :access-control-allow-methods [:get]))

(def app
  (let [the-app (reitit/ring-handler
                 (reitit/router
                  [["/" the-handler]
                   ["/localities/:state_code" {:get handlers/list-localities}]
                   ["/locality/:locality_id" {:get handlers/list-species}]])
                 (reitit/create-default-handler)
                 {:middleware [wrap-json-response
                               wrap-cors2]})]
    (wrap-defaults the-app api-defaults)))

(defn start-server []
  (jetty/run-jetty #'app {:port 8080 :join? false}))

(defstate ^:dynamic *webserver*
  :start (start-server)
  :stop (jetty/stop-server *webserver*))

(defn -main [& args]
  (mount/start))
