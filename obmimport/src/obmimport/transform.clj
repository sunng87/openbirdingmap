(ns obmimport.transform
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [next.jdbc :as jdbc]
            [obmimport.config :as conf]
            [obmimport.ebirdapi :as api]))

(defn load-ebird [file-path]
  (with-open [the-file (io/reader (io/file file-path))]
    (let [lines (csv/read-csv the-file :separator \tab)
          ;; skip the first line
          lines (doall (rest lines))]
      lines)))

;; ebird data csv
;;
;; 00. GLOBAL UNIQUE IDENTIFIER (*)
;; 01. LAST EDITED DATE
;; 02. TAXONOMIC ORDER (*)
;; 03. CATEGORY
;; 04. COMMON NAME (*)
;; 05. SCIENTIFIC NAME (*)
;; 06. SUBSPECIES COMMON NAME
;; 07. SUBSPECIES SCIENTIFIC NAME
;; 08. OBSERVATION COUNT (*)
;; 09. BREEDING CODE
;; 10. BREEDING CATEGORY
;; 11. BEHAVIOR CODE
;; 12. AGE/SEX
;; 13. COUNTRY (*)
;; 14. COUNTRY CODE (*)
;; 15. STATE (*)
;; 16. STATE CODE (*)
;; 17. COUNTY
;; 18. COUNTY CODE
;; 19. IBA CODE
;; 20. BCR CODE
;; 21. USFWS CODE
;; 22. ATLAS BLOCK
;; 23. LOCALITY (*)
;; 24. LOCALITY ID (*)
;; 25. LOCALITY TYPE (*)
;; 26. LATITUDE (*)
;; 27. LONGITUDE (*)
;; 28. OBSERVATION DATE (*)
;; 29. TIME OBSERVATIONS STARTED
;; 30. OBSERVER ID (*)
;; 31. SAMPLING EVENT IDENTIFIER
;; 32. PROTOCOL TYPE
;; 33. PROTOCOL CODE
;; 34. PROJECT CODE
;; 35. DURATION MINUTES
;; 36. EFFORT DISTANCE KM
;; 37. EFFORT AREA HA
;; 38. NUMBER OBSERVERS
;; 39. ALL SPECIES REPORTED
;; 40. GROUP IDENTIFIER
;; 41. HAS MEDIA
;; 42. APPROVED
;; 43. REVIEWED
;; 44. REASON
;; 45. TRIP COMMENTS
;; 46. SPECIES COMMENTS
;;

(defn extract-ebird-item [line]
  {:record-id (nth line 0)

   :species-id (nth line 2)
   :species-cname (nth line 4)
   :species-sname (nth line 5)

   :record-count (try (Integer/parseInt (nth line 8))
                      (catch Exception _ nil))

   :location-country (nth line 13)
   :location-country-code (nth line 14)
   :location-state (nth line 15)
   :location-state-code (nth line 16)
   :location-locality (nth line 23)
   :location-locality-id (nth line 24)
   :location-locality-type (nth line 25)
   :location-lat (nth line 26)
   :location-lon (nth line 27)

   :record-date (nth line 28)
   :record-observer-id (nth line 30)})

(def db-spec {:dbtype "mysql"
              :dbname (conf/mariadb-db-name)
              :host (conf/mariadb-db-host)
              :port (conf/mariadb-db-port)
              :user (conf/mariadb-db-username)
              :password (conf/mariadb-db-password)})

(defn datasource []
  (jdbc/get-datasource db-spec))

(defn load-ebird-taxonomy [path]
  (with-open [the-file (io/reader (io/file path))]
    (let [lines (csv/read-csv the-file :separator \,)
          lines (doall (rest lines))]
      lines)))

(defn into-id-species-code-map [ebd-taxonomy]
  (into {} (map #(vector (nth % 0) (nth % 2)) ebd-taxonomy)))

(defn dedup-by-key [k items]
  (vals (into {} (map #(vector (k %) %) items))))

(def dedup-by-location (partial dedup-by-key :location-locality-id))
(def dedup-by-species (partial dedup-by-key :species-id))

(defn insert-location! [item]
  ;; select by id before insertion
  (let [ds (datasource)
        r (jdbc/execute! ds ["select id from obm_location where id = ?" (:location-locality-id item)])]
    (when (empty? r)
      (let [params (mapv item [:location-locality-id
                               :location-locality
                               :location-lon
                               :location-lat
                               :location-country
                               :location-country-code
                               :location-state
                               :location-state-code
                               :location-locality-type])]
        (jdbc/execute! ds
                       (concat ["insert into obm_location(id, lname, lon, lat, country, country_code, state_name, state_code, ltype) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"]
                               params))))))

(defn import-species! [item id-scode-map]
  ;; select by id before insertions, too
  (let [ds (datasource)
        r (jdbc/execute! ds ["select id from obm_species where id = ?" (:species-id item)])]
    (when (empty? r)
      (let [params (mapv item [:species-id
                               :species-cname
                               :species-sname])
            scode (get id-scode-map (:species-id item))
            ;; local name from api
            lname (-> (api/call-ebird-taxonomy [scode])
                      first
                      :comName)
            params (conj params scode lname)]
        (jdbc/execute! ds
                       (concat ["insert into obm_species(id, cname, sname, species_code, local_name) values (?, ?, ?, ?, ?)"]
                                params))))))

(defn insert-record! [item]
  (let [ds (datasource)
        r (jdbc/execute! ds ["select id from obm_record where id = ?" (:record-id item)])]
    (when (empty? r)
      (let [params (mapv item [:record-id
                               :species-id
                               :location-locality-id
                               :record-date
                               :record-count
                               :record-observer-id])]
        (jdbc/execute! ds
                       (concat ["insert into obm_record(id, species_id, locality_id, record_date, record_count, observer_id) values (?, ?, ?, ?, ?, ?)"] params))))))
