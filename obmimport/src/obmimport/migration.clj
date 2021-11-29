(ns obmimport.migration
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [environ.core :refer [env]]))

(defn mariadb-db-host []
  (env :obm-db-host "localhost"))

(defn mariadb-db-port []
  (env :obm-db-port "3306"))

(defn mariadb-db-name []
  (env :obm-db-name "obm"))

(defn mariadb-db-username []
  (env :obm-db-username "obm_user"))

(defn mariadb-db-password []
  (env :obm-db-password "obm_pass"))

(def config
  (let [uri (format "jdbc:mariadb://%s:%s/%s?user=%s&password=%s"
                    (mariadb-db-host)
                    (mariadb-db-port)
                    (mariadb-db-name)
                    (mariadb-db-username)
                    (mariadb-db-password))]
    {:datastore  (jdbc/sql-database {:connection-uri uri})
     :migrations (jdbc/load-resources "migrations")}))

(defn do-migrate []
  (repl/migrate config))
