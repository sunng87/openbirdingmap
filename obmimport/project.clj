(defproject obmimport "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/tools.logging "1.3.0"]
                 [org.slf4j/slf4j-api "2.0.16"]
                 [org.slf4j/slf4j-simple "2.0.16"]

                 [dev.weavejester/ragtime.core "0.11.0"]
                 [dev.weavejester/ragtime.next-jdbc "0.11.0"]
                 [org.xerial/sqlite-jdbc "3.49.0.0"]
                 [environ "1.2.0"]
                 [org.clojure/data.csv "1.1.0"]
                 [com.github.seancorfield/next.jdbc "1.3.994"]

                 ;; http client
                 [clj-http "3.13.0"]
                 [cheshire "5.13.0"]

                 [org.clojure/tools.cli "1.0.230"]]
  :main ^:skip-aot obmimport.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}  )
