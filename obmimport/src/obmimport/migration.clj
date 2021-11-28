(ns obmimport.migration
  (:require [ragtime.jdbc :as jdbc]))

(def config
  {:datastore  (jdbc/sql-database {:connection-uri "jdbc:mariadb://localhost:3306/obm?user=obm_user&password=obm_pass"})
   :migrations (jdbc/load-resources "migrations")})
