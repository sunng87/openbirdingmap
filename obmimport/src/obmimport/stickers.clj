(ns obmimport.stickers
  "Import sticker paths and zh-Hans field-guide text from a local clone of
  https://github.com/alchaincyf/huashu-bird-sticker into obm_species_ext.

  Species are matched against obm_species by eBird species code first, with
  scientific name as a fallback (the sticker dataset follows IOC taxonomy,
  which occasionally differs from eBird/Clements)."
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn- load-sticker-catalog
  "Load data/species.csv from the sticker repo. Returns a map with two
  indexes: :by-ebird-code and :by-sname, both to {:sticker-path :score :id}."
  [repo-dir]
  (let [csv-file (io/file repo-dir "data" "species.csv")]
    (with-open [r (io/reader csv-file)]
      (let [[header & rows] (csv/read-csv r)
            header (map keyword header)
            entries (keep (fn [row]
                            (let [m (zipmap header row)]
                              (when (not-empty (:sticker_file m))
                                {:id (:id m)
                                 :ebird (not-empty (:ebird m))
                                 :sname (not-empty (:scientific_name m))
                                 :sticker-path (str/replace (:sticker_file m) #"^stickers/" "")
                                 :score (try (Integer/parseInt (:review_score m))
                                             (catch Exception _ nil))})))
                          rows)
            entries (doall entries)]
        {:by-ebird-code (into {} (keep #(when-let [code (:ebird %)] [code %]) entries))
         :by-sname (into {} (keep #(when-let [sname (:sname %)] [sname %]) entries))}))))

(defn- load-field-guides
  "Load data/field-guide.zh-Hans.json. Returns a map of Genus_species id to
  the field-guide entry."
  [repo-dir]
  (let [json-file (io/file repo-dir "data" "field-guide.zh-Hans.json")]
    (when (.isFile json-file)
      (-> (json/parse-stream (io/reader json-file))
          (get "species")))))

(defn import-stickers!
  "Import sticker metadata and field-guide text from the sticker repo at
  repo-dir into obm_species_ext. Species must already be imported."
  [datasource repo-dir]
  (let [catalog (load-sticker-catalog repo-dir)
        guides (load-field-guides repo-dir)
        species (jdbc/execute! datasource ["select id, species_code, sname from obm_species"]
                               {:builder-fn rs/as-unqualified-maps})
        stats (atom {:matched-code 0 :matched-sname 0 :unmatched 0 :guides 0})]
    (jdbc/with-transaction [tx datasource]
      (doseq [sp species]
        (let [by-code (get (:by-ebird-code catalog) (:species_code sp))
              by-sname (get (:by-sname catalog) (:sname sp))
              entry (or by-code by-sname)]
          (if entry
            (do
              (swap! stats update (if by-code :matched-code :matched-sname) inc)
              (let [guide (get guides (:id entry))]
                (when guide (swap! stats update :guides inc))
                (jdbc/execute! tx
                               ["insert into obm_species_ext(species_id, sticker_path, sticker_score, field_guide)
                                 values (?, ?, ?, ?)
                                 on conflict(species_id) do update set
                                   sticker_path = excluded.sticker_path,
                                   sticker_score = excluded.sticker_score,
                                   field_guide = excluded.field_guide"
                                (:id sp)
                                (:sticker-path entry)
                                (:score entry)
                                (when guide (json/generate-string guide))])))
            (swap! stats update :unmatched inc)))))
    (log/infof "Sticker import: %d matched by ebird code, %d by scientific name, %d unmatched, %d with field guide"
               (:matched-code @stats) (:matched-sname @stats) (:unmatched @stats) (:guides @stats))
    @stats))
