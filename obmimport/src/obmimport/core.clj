(ns obmimport.core
  (:gen-class)
  (:require
   [environ.core :refer [env]]
   [clojure.tools.logging :as log]
   [clojure.tools.cli :as cli]
   [next.jdbc :as jdbc]
   [obmimport.migration :as mg]
   [obmimport.transform :as im]))

(def cli-option
  [["-d" "--data NAME" "ebird files to import"
    :multi true
    :default []
    :update-fn conj]
   [nil "--skip-migration" "skip database migration"]])

(defn do-import! [paths]
  (let [datasource (im/datasource)]
    ;; WAL persists on the db file and allows concurrent readers (the API
    ;; server) while importing
    (jdbc/execute! datasource ["PRAGMA journal_mode=WAL"])
    (doseq [path paths]
      (log/infof "Import ebird data %s" path)
      (let [ebird-data (mapv im/extract-ebird-item (im/load-ebird path))
            locations (im/dedup-by-location ebird-data)
            species (im/dedup-by-species ebird-data)]
        ;; import each file atomically in a single transaction; this is also
        ;; orders of magnitude faster than per-statement autocommit on sqlite
        (jdbc/with-transaction [tx datasource]
          (binding [im/*ds* tx]
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
            (log/infof "Total ebird items %d" (count ebird-data))
            ;; record region metadata for each state covered by this file
            (doseq [[state-code items] (group-by :location-state-code ebird-data)]
              (when (not-empty state-code)
                (log/infof "Record metadata for region %s" state-code)
                (im/upsert-metadata! state-code items)))))))))

(defn -main [& args]
  (log/info "Update database schema to latest version.")
  (let [options (cli/parse-opts args cli-option)]
    (if-not (:errors options)
      (do
        (let [options (:options options)]
          ;; db migration
          (when-not (:skip-migration options)
            (mg/do-migrate!))

          ;; data import
          (let [paths (:data options)]
            (log/infof "Import data using files %s" paths)
            (when (empty? paths)
              (log/info "No data file specified")
              (System/exit 0))

            (when (empty? (env :ebird-api-key))
              (log/error "Invalid ebird api key")
              (System/exit 1))

            (do-import! paths))))
      ;; invalid cli options
      (do
        (log/errorf "Error parsing cli options %s" (pr-str (:errors options)))
        (System/exit 1)))))
