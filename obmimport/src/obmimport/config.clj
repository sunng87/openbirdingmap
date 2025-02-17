(ns obmimport.config
  (:require [environ.core :refer [env]]))

(defn db-file []
  (env :obm-dbfile "obm.db"))

(def db-spec {:classname   "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname (db-file)})
