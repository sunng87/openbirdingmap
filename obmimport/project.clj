(defproject obmimport "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.0"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.slf4j/slf4j-api "2.0.11"]
                 [org.slf4j/slf4j-simple "2.0.11"]

                 [dev.weavejester/ragtime.core "0.9.3"]
                 [dev.weavejester/ragtime.next-jdbc "0.9.3"]
                 [org.mariadb.jdbc/mariadb-java-client "3.3.2"]
                 [environ "1.2.0"]
                 [org.clojure/data.csv "1.0.0"]
                 [com.github.seancorfield/next.jdbc "1.3.909"]

                 ;; http client
                 [clj-http "3.12.3"]
                 [cheshire "5.12.0"]

                 [org.clojure/tools.cli "1.0.219"]]
  :main ^:skip-aot obmimport.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}  )
