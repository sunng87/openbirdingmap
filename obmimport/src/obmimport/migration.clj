(ns obmimport.migration
  (:require [ragtime.next-jdbc :as jdbc]
            [ragtime.repl :as repl]
            [obmimport.config :as conf]))

(defn load-config! []
  {:datastore  (jdbc/sql-database conf/db-spec)
   :migrations (jdbc/load-resources "migrations")})

(defn do-migrate! []
  (repl/migrate (load-config!)))
