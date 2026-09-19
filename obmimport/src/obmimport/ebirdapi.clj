(ns obmimport.ebirdapi
  (:require
   [environ.core :refer [env]]
   [clj-http.client :as http]))

(defn fetch-full-taxonomy
  "Fetch the full eBird taxonomy (localized common names) in a single call.
  Per-species queries are not viable: the api rate limit is ~500 calls/hour,
  below the species count of a typical regional dataset."
  []
  (let [url "https://api.ebird.org/v2/ref/taxonomy/ebird"
        resp (http/get url {:query-params {:locale "zh_SIM"
                                           :fmt "json"}
                            :as :json
                            :headers {"X-eBirdApiToken" (env :ebird-api-key)}})]
    (:body resp)))
