(ns obmserver.core
  (:gen-class)
  (:require [ring.adapter.jetty9 :as jetty]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :as resp]
            [reitit.ring :as reitit]))

(defn the-handler [req]
  (println 123)
  (resp/response {:data "ok"}))

(def app
  (reitit/ring-handler
   (reitit/router
    ["/" the-handler])
   (reitit/create-default-handler)
   {:middleware [wrap-json-response]}))

(defn start-server []
  (jetty/run-jetty #'app {:port 8080 :join? false}))

(defn -main
  [& args]
  (start-server))
