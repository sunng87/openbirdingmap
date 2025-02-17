(defproject obmserver "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 ;; web
                 [info.sunng/ring-jetty9-adapter "0.36.1"]
                 [ring/ring-core "1.13.0"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-defaults "0.6.0"]
                 [metosin/reitit-ring "0.7.2"]
                 [ring-cors "0.1.13"]
                 [ring-logger "1.1.1"]
                 ;; database
                 [conman "0.9.6"]
                 [com.layerware/hugsql-core "0.5.3"]
                 [com.layerware/hugsql-adapter-next-jdbc "0.5.3"]
                 [org.xerial/sqlite-jdbc "3.49.0.0"]
                 ;; config
                 [stavka "0.7.0"]
                 ;; lifecycle
                 [mount "0.1.21"]
                 ;; logging
                 [org.clojure/tools.logging "1.3.0"]
                 [org.slf4j/slf4j-api "2.0.16"]
                 [org.apache.logging.log4j/log4j-api "2.24.3"]
                 [org.apache.logging.log4j/log4j-core "2.24.3"]
                 [org.apache.logging.log4j/log4j-slf4j2-impl "2.24.3"]
                 ;; web crawling
                 [clj-http "3.13.0"]
                 [org.jsoup/jsoup "1.18.2"]
                 ;; parallel processing
                 [manifold "0.4.3"]]
  :main ^:skip-aot obmserver.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
