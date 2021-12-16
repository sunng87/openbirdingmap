(defproject obmimport "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [dev.weavejester/ragtime "0.9.0"]
                 [org.mariadb.jdbc/mariadb-java-client "2.7.4"]
                 [environ "1.2.0"]
                 [org.clojure/data.csv "1.0.0"]
                 [seancorfield/next.jdbc "1.2.659"]

                 ;; http client
                 [clj-http "3.12.3"]
                 [cheshire "5.10.1"]])
