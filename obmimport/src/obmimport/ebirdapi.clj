(ns obmimport.ebirdapi
  (:require
   [clojure.string :as string]
   [environ.core :refer [env]]
   [clj-http.client :as http]))

(defn call-ebird-taxonomy [species-codes]
  (let [url "https://api.ebird.org/v2/ref/taxonomy/ebird"
        resp (http/get url {:query-params {:species (string/join "," species-codes)
                                           :locale "zh_SIM"
                                           :fmt "json"}
                            :as :json
                            :headers {"X-eBirdApiToken" (env :ebird-api-key)}})]
    (:body resp)))
