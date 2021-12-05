(ns obmimport.migration
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [obmimport.config :as conf]))

(defn load-config! []
  (let [uri (format "jdbc:mariadb://%s:%s/%s?user=%s&password=%s"
                    (conf/mariadb-db-host)
                    (conf/mariadb-db-port)
                    (conf/mariadb-db-name)
                    (conf/mariadb-db-username)
                    (conf/mariadb-db-password))]
    {:datastore  (jdbc/sql-database {:connection-uri uri})
     :migrations (jdbc/load-resources "migrations")}))

(defn do-migrate! []
  (repl/migrate (load-config!)))
