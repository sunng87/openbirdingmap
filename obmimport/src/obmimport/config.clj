(ns obmimport.config
  (:require [environ.core :refer [env]]))

(defn sqlite-db-file []
  (env :obm-db-file "obm.db"))

(def db-spec {:dbtype "sqlite"
              :dbname (sqlite-db-file)})
