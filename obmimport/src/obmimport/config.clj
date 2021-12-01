(ns obmimport.config
  (:require [environ.core :refer [env]]))

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
