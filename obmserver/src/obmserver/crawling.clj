(ns obmserver.crawling
  (:require [clojure.string :as string]
            [clj-http.client :as http])
  (:import [org.jsoup Jsoup]))

(defn to-ebird-url [species-id]
  (format "https://ebird.org/species/%s" species-id))

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
        images (.select soup "div.MediaFeed:first-of-type div.MediaThumbnail img")
        labels (.select soup "div.MediaFeed:first-of-type h3.MediaFeedItem-title")]
    (mapv #(hash-map :src (.attr %1 "src") :alt (.attr %1 "alt") :title (.text %2)) images labels)))

(defn images [species-id]
  (-> (to-ebird-url species-id)
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
