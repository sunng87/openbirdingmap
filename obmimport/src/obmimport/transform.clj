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

;; ebird data csv: relNov-2022
;;
;; 00. GLOBAL UNIQUE IDENTIFIER (*)
;; 01. LAST EDITED DATE
;; 02. TAXONOMIC ORDER (*)
;; 03. CATEGORY
;; 04. TAXON CONCEPT ID
;; 05. COMMON NAME (*)
;; 06. SCIENTIFIC NAME (*)
;; 07. SUBSPECIES COMMON NAME
;; 08. SUBSPECIES SCIENTIFIC NAME
;; 09. EXOTIC CODE
;; 10. OBSERVATION COUNT (*)
;; 11. BREEDING CODE
;; 12. BREEDING CATEGORY
;; 13. BEHAVIOR CODE
;; 14. AGE/SEX
;; 15. COUNTRY (*)
;; 16. COUNTRY CODE (*)
;; 17. STATE (*)
;; 18. STATE CODE (*)
;; 19. COUNTY
;; 20. COUNTY CODE
;; 21. IBA CODE
;; 22. BCR CODE
;; 23. USFWS CODE
;; 24. ATLAS BLOCK
;; 25. LOCALITY (*)
;; 26. LOCALITY ID (*)
;; 27. LOCALITY TYPE (*)
;; 28. LATITUDE (*)
;; 29. LONGITUDE (*)
;; 30. OBSERVATION DATE (*)
;; 31. TIME OBSERVATIONS STARTED
;; 32. OBSERVER ID (*)
;; 33. SAMPLING EVENT IDENTIFIER
;; 34. PROTOCOL TYPE
;; 35. PROTOCOL CODE
;; 36. PROJECT CODE
;; 37. DURATION MINUTES
;; 38. EFFORT DISTANCE KM
;; 39. EFFORT AREA HA
;; 40. NUMBER OBSERVERS
;; 41. ALL SPECIES REPORTED
;; 42. GROUP IDENTIFIER
;; 43. HAS MEDIA
;; 44. APPROVED
;; 45. REVIEWED
;; 46. REASON
;; 47. TRIP COMMENTS
;; 48. SPECIES COMMENTS
;;

(defn extract-ebird-item [line]
  {:record-id (nth line 0)

   :species-id (nth line 2)
   :species-cname (nth line 5)
   :species-sname (nth line 6)

   :record-count (try (Integer/parseInt (nth line 10))
                      (catch Exception _ nil))

   :location-country (nth line 15)
   :location-country-code (nth line 16)
   :location-state (nth line 17)
   :location-state-code (nth line 18)
   :location-locality (nth line 25)
   :location-locality-id (nth line 26)
   :location-locality-type (nth line 27)
   :location-lat (nth line 28)
   :location-lon (nth line 29)

   :record-date (nth line 30)
   :record-observer-id (nth line 32)})

(defn datasource []
  (jdbc/get-datasource conf/db-spec))

;; TODO: make this built-in
(defn into-id-species-code-map [ebd-taxonomy]
  (into {} (map #(vector (nth % 0) (nth % 2)) ebd-taxonomy)))

(def ebird-taxonomy-dict
  (with-open [the-file (io/reader (io/resource "ebird_taxonomy_v2022.csv"))]
    (let [lines (csv/read-csv the-file :separator \,)
          lines (doall (rest lines))]
      (into-id-species-code-map lines))))

(defn dedup-by-key [k items]
  (vals (into {} (map #(vector (k %) %) items))))

(def dedup-by-location (partial dedup-by-key :location-locality-id))
(def dedup-by-species (partial dedup-by-key :species-id))

(declare ^:dynamic *ds*)

(defn insert-location! [item]
  ;; select by id before insertion
  (let [r (jdbc/execute! *ds* ["select id from obm_location where id = ?" (:location-locality-id item)])]
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
        (jdbc/execute! *ds*
                       (concat ["insert into obm_location(id, lname, lon, lat, country, country_code, state_name, state_code, ltype) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"]
                               params))))))

(defn import-species! [item]
  ;; select by id before insertions, too
  (let [r (jdbc/execute! *ds* ["select id from obm_species where id = ?" (:species-id item)])]
    (when (empty? r)
      (let [params (mapv item [:species-id
                               :species-cname
                               :species-sname])
            scode (get ebird-taxonomy-dict (:species-id item))
            ;; local name from api
            lname (-> (api/call-ebird-taxonomy [scode])
                      first
                      :comName)
            params (conj params scode lname)]
        (jdbc/execute! *ds*
                       (concat ["insert into obm_species(id, cname, sname, species_code, local_name) values (?, ?, ?, ?, ?)"]
                                params))))))

(defn insert-record! [item]
  (let [r (jdbc/execute! *ds* ["select id from obm_record where id = ?" (:record-id item)])]
    (when (empty? r)
      (let [params (mapv item [:record-id
                               :species-id
                               :location-locality-id
                               :record-date
                               :record-count
                               :record-observer-id])]
        (jdbc/execute! *ds*
                       (concat ["insert into obm_record(id, species_id, locality_id, record_date, record_count, observer_id) values (?, ?, ?, ?, ?, ?)"] params))))))
