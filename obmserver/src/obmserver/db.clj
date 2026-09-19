(ns obmserver.db
  (:require [conman.core :as conman]
            [mount.core :refer [defstate]]
            [stavka.core :refer [$$]]))

(defn sqlite-db-file []
  ($$ :obm.db.file "obm.db"))

(defn pool-spec []
  {:jdbc-url (format "jdbc:sqlite:%s" (sqlite-db-file))})

(defstate ^:dynamic *db*
  :start (conman/connect! (pool-spec))
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "queries.sql")
