(defproject obmserver "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 ;; web
                 [info.sunng/ring-jetty9-adapter "0.16.0"]
                 [ring/ring-core "1.9.4"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-defaults "0.3.3"]
                 [metosin/reitit-ring "0.5.15"]
                 [ring-cors "0.1.13"]
                 ;; database
                 [conman "0.9.1"]
                 [com.layerware/hugsql "0.5.1"]
                 [org.mariadb.jdbc/mariadb-java-client "2.7.4"]
                 ;; config
                 [environ "1.2.0"]
                 ;; lifecycle
                 [mount "0.1.16"]]
  :main ^:skip-aot obmserver.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
