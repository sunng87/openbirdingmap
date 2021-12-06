(ns obmserver.db
  (:require [conman.core :as conman]
            [mount.core :refer [defstate]]
            [environ.core :refer [env]]))

(defn mariadb-db-host []
  (env :obm-db-host "localhost"))

(defn mariadb-db-port []
  (Integer/parseInt (env :obm-db-port "3306")))

(defn mariadb-db-name []
  (env :obm-db-name "obm"))

(defn mariadb-db-username []
  (env :obm-db-username "obm_user"))

(defn mariadb-db-password []
  (env :obm-db-password "obm_pass"))

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
