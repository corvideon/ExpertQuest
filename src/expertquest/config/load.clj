(ns expertquest.config.load
  "ExpertQuest configuration loading functions"
  (:require clojure.edn)
  (:require local-file))

(defn- load-config [config-file]
  (clojure.edn/read-string (slurp config-file)))

;; load the tweet config file and store in a var
(def twitter-config
  (load-config (local-file/file* "/config/twitter-api-config.edn")))

;; load the github config file and store in a var
(def github-config
  (load-config (local-file/file* "/config/github-api-config.edn")))

;; load the tiobe top 50 data
(def tiobe-data
  (load-config (local-file/file* "/resources/data/tiobe-top-50.edn")))