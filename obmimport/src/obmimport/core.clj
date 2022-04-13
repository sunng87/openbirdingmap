(ns obmimport.core
  (:gen-class)
  (:require
   [environ.core :refer [env]]
   [clojure.tools.logging :as log]
   [clojure.tools.cli :as cli]
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
          (log/infof "Total ebird items %d" (count ebird-data)))))))

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
