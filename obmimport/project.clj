(defproject obmimport "0.1.0-SNAPSHOT"
  :description "convert ebd data to obm sqlite db file"
  :url "https://github.com/sunng87/openbirdingmap"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.6"]
                 [org.clojure/tools.logging "1.3.1"]
                 [org.slf4j/slf4j-api "2.0.19"]
                 [org.slf4j/slf4j-simple "2.0.19"]

                 [dev.weavejester/ragtime.core "0.12.1"]
                 [dev.weavejester/ragtime.next-jdbc "0.12.1"]
                 [org.xerial/sqlite-jdbc "3.53.4.0"]
                 [environ "1.2.0"]
                 [org.clojure/data.csv "1.1.1"]
                 [com.github.seancorfield/next.jdbc "1.3.1118"]

                 ;; http client
                 [clj-http "3.13.1"]
                 [cheshire "6.2.0"]

                 [org.clojure/tools.cli "1.1.256"]]
  :main ^:skip-aot obmimport.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}  )
