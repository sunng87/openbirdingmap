(ns obmserver.crawling
  (:require [clojure.string :as string]
            [clj-http.client :as http])
  (:import [org.jsoup Jsoup]))

(defn to-ebird-url [species-id region-id]
  (format "https://ebird.org/species/%s/%s" species-id region-id))

(defn fetch-html [url]
  (let [r (http/get url {:socket-timeout 5000
                         :connection-timeout 2000})]
    (when (= (:status r) 200)
      (:body r))))

(defn parse-head-image [html]
  (let [soup (Jsoup/parse ^String html)
        image-meta (.. soup (select "meta[property=og:image]") (first) (attr "content"))
        image-info-meta (.. soup (select "meta[property=og:image:alt]") (first) (attr "content"))]
    {:image {:src image-meta :alt image-info-meta}}))
