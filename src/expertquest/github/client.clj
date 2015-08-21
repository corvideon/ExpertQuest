(ns expertquest.github.client
  "ExpertQuest GitHub client library"
  (:require [expertquest.config.load :as config])
  (:require [clj-http.client :as http-client])
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as string])
  (:require [tentacles.users :as github-users])
  (:require [tentacles.repos :as github-repos]))

(defn get-github-user-data
  "Gets details of a GitHub user"
  [github-username]
  (github-users/user github-username {:auth (:auth config/github-config)}))

(defn get-github-user-repos
  "List the repos of the GitHib user"
  [github-username]
  (github-repos/user-repos github-username {:auth (:auth config/github-config)}))

(defn get-github-user-boc
  "Counts the bytes of code in <prog-lang> for <github-username>"
  [github-username prog-lang]
  (apply +
      (filter #(not= nil %1)
           (map #(get %1 (string/capitalize prog-lang) )
                 (map #(json/read-str (:body
                     (http-client/get (:languages_url %1) {:accept :json :basic-auth (:auth config/github-config)})))
                          (get-github-user-repos github-username))))))




