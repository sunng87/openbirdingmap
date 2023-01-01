(defproject obmserver "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 ;; web
                 [info.sunng/ring-jetty9-adapter "0.18.3"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-defaults "0.3.4"]
                 [metosin/reitit-ring "0.5.18"]
                 [ring-cors "0.1.13"]
                 [ring-logger "1.1.1"]
                 ;; database
                 [conman "0.9.6"]
                 [com.layerware/hugsql-core "0.5.3"]
                 [com.layerware/hugsql-adapter-next-jdbc "0.5.3"]
                 [org.mariadb.jdbc/mariadb-java-client "3.1.0"]
                 ;; config
                 [stavka "0.7.0"]
                 ;; lifecycle
                 [mount "0.1.17"]
                 ;; logging
                 [org.clojure/tools.logging "1.2.4"]
                 [org.slf4j/slf4j-api "2.0.5"]
                 [org.apache.logging.log4j/log4j-api "2.19.0"]
                 [org.apache.logging.log4j/log4j-core "2.19.0"]
                 [org.apache.logging.log4j/log4j-slf4j2-impl "2.19.0"]
                 ;; web crawling
                 [clj-http "3.12.3"]
                 [org.jsoup/jsoup "1.15.3"]
                 ;; parallel processing
                 [manifold "0.3.0"]]
  :main ^:skip-aot obmserver.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
