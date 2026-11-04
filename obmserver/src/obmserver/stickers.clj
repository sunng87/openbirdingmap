(ns obmserver.stickers
  "Runtime resolver for the huashu-bird-sticker dataset
  (https://github.com/alchaincyf/huashu-bird-sticker).

  The dataset is a pure runtime dependency: at startup we load
  data/species.csv (sticker paths, matched by eBird species code with
  scientific name as fallback) and data/field-guide.zh-Hans.json from a
  local checkout, and resolve per-species sticker/field-guide info at
  request time. Nothing is stored in the obm database.

  Since sticker data 2.0.0 the dataset follows AviList v2026, which
  occasionally differs from our eBird taxonomy: besides eBird code and
  scientific name we also match `previous_ioc_ids` (old IOC ids of
  renamed/lumped species), and when a species was lumped into another we
  prefer its subspecies-form sticker (and field guide) over the parent's.

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

(defn- split-pipe [s]
  (when (not-empty s)
    (str/split s #"\|")))

(defn- load-catalog
  "Load data/species.csv from the sticker repo. Returns three indexes:
  :by-ebird-code, :by-sname and :by-prev-id (old IOC ids), all to
  {:id :sname :sticker-path :forms}."
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
                                  :sticker-path (str/replace (:sticker_file m) #"^stickers/" "")
                                  :prev-ids (split-pipe (:previous_ioc_ids m))
                                  ;; pipe-separated trinomials, e.g. "Anas crecca carolinensis"
                                  :forms (split-pipe (:forms m))})))
                           rows))]
        {:by-ebird-code (into {} (keep #(when-let [code (:ebird %)] [code %]) entries))
         :by-sname (into {} (keep #(when-let [sname (:sname %)] [sname %]) entries))
         :by-prev-id (into {} (for [e entries, p (:prev-ids e)] [p e]))}))))

(defn- load-field-guides
  "Load data/field-guide.zh-Hans.json. Returns a map of taxon id
  (Genus_species, or Genus_species.subspecies for forms) to the
  field-guide entry, or nil when the file is absent."
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
        (log/infof "Loaded sticker catalog from %s: %d by ebird code, %d by scientific name, %d by previous IOC id, %d field guides"
                   dir (count (:by-ebird-code catalog)) (count (:by-sname catalog))
                   (count (:by-prev-id catalog)) (count guides))
        {:catalog catalog :guides guides})
      (catch Exception e
        (log/warnf e "Failed to load sticker data from %s; stickers disabled" dir)
        nil))
    (log/info "Sticker repo not configured (:obm.sticker.dir unset); stickers disabled")))

(defstate ^:dynamic *stickers*
  :start (load-all)
  :stop nil)

(defn- form-epithet
  "When the queried species sname (e.g. \"Anas carolinensis\") was lumped
  into the matched entry (e.g. Anas crecca, forms \"Anas crecca
  carolinensis\"), return the subspecies epithet (\"carolinensis\") of the
  matching form, else nil."
  [sname entry]
  (when (and (not= sname (:sname entry)) (seq (:forms entry)))
    (let [genus (first (str/split sname #" "))
          epithet (last (str/split sname #" "))]
      (some (fn [form]
              ;; form is a trinomial like "Anas crecca carolinensis"
              (let [[f-genus _f-species f-epithet] (str/split form #" ")]
                (when (and (= f-genus genus) (= f-epithet epithet))
                  epithet)))
            (:forms entry)))))

(defn species-ext
  "Resolve sticker path and field-guide entry for a species by its eBird
  species code, falling back to scientific name and then to old IOC ids
  (the sticker dataset follows AviList since data 2.0.0, which sometimes
  differs from eBird taxonomy). When the species was lumped into another
  one, its subspecies-form sticker/guide is preferred."
  [species-code sname]
  (when-let [{:keys [catalog guides]} *stickers*]
    (when-let [entry (or (get (:by-ebird-code catalog) species-code)
                         (get (:by-sname catalog) sname)
                         (get (:by-prev-id catalog) (str/replace sname #" " "_")))]
      (if-let [epithet (form-epithet sname entry)]
        ;; lumped species: use the form's sticker and field guide
        {:sticker-path (str/replace (:sticker-path entry) #"\.webp$" (str "." epithet ".webp"))
         :field-guide (get guides (str (:id entry) "." epithet))}
        (cond-> {:sticker-path (:sticker-path entry)}
          (get guides (:id entry))
          (assoc :field-guide (get guides (:id entry))))))))
