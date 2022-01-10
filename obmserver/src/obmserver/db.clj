(ns obmserver.db
  (:require [conman.core :as conman]
            [mount.core :refer [defstate]]
            [stavka.core :refer [$$ $$l]]))

(defn mariadb-db-host []
  ($$ :obm.db.host "localhost"))

(defn mariadb-db-port []
  ($$l :obm.db.port 3306))

(defn mariadb-db-name []
  ($$ :obm.db.name "obm"))

(defn mariadb-db-username []
  ($$ :obm.db.username "obm_user"))

(defn mariadb-db-password []
  ($$ :obm.db.password "obm_pass"))

(defn pool-spec []
  {:jdbc-url (format "jdbc:mariadb://%s:%s/%s?user=%s&password=%s"
                     (mariadb-db-host)
                     (mariadb-db-port)
                     (mariadb-db-name)
                     (mariadb-db-username)
                     (mariadb-db-password))})

(defstate ^:dynamic *db*
  :start (conman/connect! (pool-spec))
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "queries.sql")
