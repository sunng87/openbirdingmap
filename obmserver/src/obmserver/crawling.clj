(ns obmserver.crawling
  (:require [clojure.string :as string]
            [clj-http.client :as http])
  (:import [org.jsoup Jsoup]))

(defn to-bow-url [species-id]
  (format "https://birdsoftheworld.org/bow/species/%s/cur/introduction" species-id))

(defn fetch-html [url]
  (let [r (http/get url {:socket-timeout 5000
                         :connection-timeout 3000})]
    (when (= (:status r) 200)
      (:body r))))

(defn parse-head-image [html]
  (let [soup (Jsoup/parse ^String html)
        image-meta (.. soup (select "meta[property=og:image]") (first) (attr "content"))
        image-info-meta (.. soup (select "meta[property=og:image:alt]") (first) (attr "content"))]
    {:src image-meta :alt image-info-meta}))

(defn parse-body-images [html]
  (let [soup (Jsoup/parse ^String html)
        items (.select soup "a[data-media-type=photo]")]
    (mapv #(hash-map :src (.attr % "data-asset-src")
                     :title (.attr % "data-asset-title")
                     :author (.attr % "data-asset-username")
                     :citation (.attr % "data-asset-citationname")
                     :state (.attr % "data-asset-stateprov")
                     :country (.attr % "data-asset-country")
                     :link (.attr % "data-asset-mllink"))
          items)))

(defn images [species-id]
  (-> (to-bow-url species-id)
      fetch-html
      parse-body-images))

(defn query-xeno-canto-url [sname]
  (format "https://www.xeno-canto.org/api/2/recordings?query=%s"
          (-> sname (string/replace #" |-" "+"))))

(defn- fetch-json [url]
  (let [r (http/get url {:socket-timeout 5000
                         :connection-timeout 3000
                         :as :json})]
    (when (= (:status r) 200)
      (:body r))))

(defn parse-recordings [data]
  (when-let [recordings (not-empty (:recordings data))]
    (take 5 (map #(select-keys % [:file :cnt :rec :loc :length :date :type :sono :id]) recordings))))

(defn recordings [species-name]
  (-> (query-xeno-canto-url species-name)
      fetch-json
      parse-recordings))
