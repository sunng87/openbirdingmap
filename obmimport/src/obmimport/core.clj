(ns obmimport.core
  (:gen-class)
  (:require
   [environ.core :refer [env]]
   [clojure.tools.logging :as log]
   [obmimport.migration :as mg]
   [obmimport.transform :as im]))

(defn -main [& args]
  (log/info "Update database schema to latest version.")
  (mg/do-migrate!)
  (let [paths args]
    (log/infof "Import data using files %s" paths)
    (when (empty? paths)
      (log/error "Invalid path")
      (System/exit 1))

    (when (empty? (env :ebird-api-key))
      (log/error "Invalid ebird api key")
      (System/exit 1))

    (let [datasource (im/datasource)]
      (binding [im/*ds* datasource]
        (doseq [path paths]
          (log/infof "Import ebird data %s" path)
          (let [ebird-data (mapv im/extract-ebird-item (im/load-ebird path))
                locations (im/dedup-by-location ebird-data)
                species (im/dedup-by-species ebird-data)]
            (doseq [l locations]
              (log/infof "Import location %s" (:location-locality l))
              (im/insert-location! l))
            (log/infof "Total locations imported %d" (count locations))
            (doseq [s species]
              (log/infof "Import species %s" (:species-cname s))
              (im/import-species! s))
            (log/infof "Total species imported %d" (count species))
            (doseq [o ebird-data]
              (im/insert-record! o))
            (log/infof "Total ebird items %d" (count ebird-data))))))))
