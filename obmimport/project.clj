(defproject obmimport "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.logging "1.2.3"]
                 [org.slf4j/slf4j-api "1.7.32"]
                 [org.slf4j/slf4j-simple "1.7.32"]

                 [dev.weavejester/ragtime.core "0.9.0"]
                 [dev.weavejester/ragtime.next-jdbc "0.9.0"]
                 [org.mariadb.jdbc/mariadb-java-client "2.7.4"]
                 [environ "1.2.0"]
                 [org.clojure/data.csv "1.0.0"]
                 [com.github.seancorfield/next.jdbc "1.2.761"]

                 ;; http client
                 [clj-http "3.12.3"]
                 [cheshire "5.10.1"]]
  :main ^:skip-aot obmimport.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}  )
