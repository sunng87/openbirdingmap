(ns obmserver.stickers
  "Runtime resolver for the huashu-bird-sticker dataset
  (https://github.com/alchaincyf/huashu-bird-sticker).

  The dataset is a pure runtime dependency: at startup we load
  data/species.csv (sticker paths, matched by eBird species code with
  scientific name as fallback) and data/field-guide.zh-Hans.json from a
  local checkout, and resolve per-species sticker/field-guide info at
  request time. Nothing is stored in the obm database.

  Configure with :obm.sticker.dir (nested {:obm {:sticker {:dir ...}}} in
  config.edn, or the OBM_STICKER_DIR env var) pointing at the repo root,
  i.e. the directory containing stickers/ and data/."
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [mount.core :refer [defstate]]
            [stavka.core :refer [$$]]))

(defn sticker-repo-dir []
  ($$ :obm.sticker.dir nil))

(defn- load-catalog
  "Load data/species.csv from the sticker repo. Returns two indexes:
  :by-ebird-code and :by-sname, both to {:id :sticker-path}."
  [repo-dir]
  (let [csv-file (io/file repo-dir "data" "species.csv")]
    (with-open [r (io/reader csv-file)]
      (let [[header & rows] (csv/read-csv r)
            header (map keyword header)
            entries (doall
                     (keep (fn [row]
                             (let [m (zipmap header row)]
                               (when (not-empty (:sticker_file m))
                                 {:id (:id m)
                                  :ebird (not-empty (:ebird m))
                                  :sname (not-empty (:scientific_name m))
                                  :sticker-path (str/replace (:sticker_file m) #"^stickers/" "")})))
                           rows))]
        {:by-ebird-code (into {} (keep #(when-let [code (:ebird %)] [code %]) entries))
         :by-sname (into {} (keep #(when-let [sname (:sname %)] [sname %]) entries))}))))

(defn- load-field-guides
  "Load data/field-guide.zh-Hans.json. Returns a map of Genus_species id to
  the field-guide entry, or nil when the file is absent."
  [repo-dir]
  (let [json-file (io/file repo-dir "data" "field-guide.zh-Hans.json")]
    (when (.isFile json-file)
      (-> (json/parse-stream (io/reader json-file))
          (get "species")))))

(defn- load-all []
  (if-let [dir (sticker-repo-dir)]
    (try
      (let [catalog (load-catalog dir)
            guides (load-field-guides dir)]
        (log/infof "Loaded sticker catalog from %s: %d by ebird code, %d by scientific name, %d field guides"
                   dir (count (:by-ebird-code catalog)) (count (:by-sname catalog)) (count guides))
        {:catalog catalog :guides guides})
      (catch Exception e
        (log/warnf e "Failed to load sticker data from %s; stickers disabled" dir)
        nil))
    (log/info "Sticker repo not configured (:obm.sticker.dir unset); stickers disabled")))

(defstate ^:dynamic *stickers*
  :start (load-all)
  :stop nil)

(defn species-ext
  "Resolve sticker path and field-guide entry for a species by its eBird
  species code, falling back to scientific name (the sticker dataset
  follows IOC taxonomy, which occasionally differs from eBird/Clements)."
  [species-code sname]
  (when-let [{:keys [catalog guides]} *stickers*]
    (when-let [entry (or (get (:by-ebird-code catalog) species-code)
                         (get (:by-sname catalog) sname))]
      (cond-> {:sticker-path (:sticker-path entry)}
        (get guides (:id entry))
        (assoc :field-guide (get guides (:id entry)))))))
