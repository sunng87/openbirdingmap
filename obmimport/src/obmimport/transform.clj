(ns obmimport.transform
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [next.jdbc :as jdbc]
            [obmimport.config :as conf]))

(defn load-ebird [file-path]
  (let [the-file (io/reader (io/file file-path))
        lines (csv/read-csv the-file :separator \tab)
        ;; skip the first line
        lines (rest lines)]
    lines))

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

(defn try-insert-location [item]
  )

(defn try-insert-species [item])

(defn try-insert-record [item])
