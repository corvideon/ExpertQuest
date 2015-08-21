(ns expertquest.search.core
  "ExpertQuest search functions"
  (:require [expertquest.twitter.client :as twitter])
  (:require [expertquest.dbpedia.client :as dbpedia])
  (:require [expertquest.github.client :as github])
  (:require [expertquest.nlp.textanalysis :as nlp])
  (:require [expertquest.config.load :as config]))

"The amount of search items to load from Twitter"
(def ^:const twitter-search-count 50)

"The amount of tweets to load from a users Twitter timeline"
(def ^:const user-timeline-count 25)


(defn  do-fvec-dbpedia
  "Looks a term up on DBPedia and gets a feature hash vector for it"
  [lookup-term]
  (nlp/get-fvec-for-text (dbpedia/get-dbpedia-abstract lookup-term)))

(defn  do-fvec-tweets
  "Gets <count> number of tweets for account <screen-name> and gets a feature hash vector for the text of the tweets"
  [screen-name count]
  (nlp/get-fvec-for-text (clojure.string/join " " (expertquest.twitter.client/get-user-tweet-texts screen-name count))))

(defn  do-twitter-search
  "Searches Twitter via the search api"
  [lookup-term count]
  (twitter/search-twitter lookup-term count))

(defn  do-cosim
  "Gets a cosine similarity for vectors <a> and <b>"
  [a b]
  (nlp/cosine-similarity a b))

(defn get-github-followers?
  "Returns -1 if there is no GH account, otherwise the number of followers"
  [username]
  (let [github-data (github/get-github-user-data username)]
    (if (= "Not Found" (:message (:body github-data)))
      -1
     (:followers github-data))))


(defn get-tweet-cosim
  "Gets a cosine similarity for a DBPedia abtract and a users tweets"
  [screen-name count abstract-fvec]
  (do-cosim (do-fvec-tweets screen-name count) abstract-fvec))

(defn structure-tweet-data
  "Builds a tweet hash"
  [twitter-search-data]
  (into #{} (map #(assoc {} :screen_name (:screen_name %1) :user  (:user %1) :twitter-followers (:followers %1)) twitter-search-data)))

(defn filter-tweets-by-gh
  "Filters tweet data by whether the user has a GitHub account, also returns the number of GH followers"
  [tweets]
  (filter #(not= -1 (:github-followers %1))
          (map
                #(let [github-followers (get-github-followers? (:screen_name %1))]
                  (into %1 (assoc {} :github-followers github-followers))) tweets))
  )

(defn get-tiobe-terms
  "Loads the map containing data about a dbpedia term"
  []
  (identity config/tiobe-data)
  )

(defn do-search
  "Main search algorithm"
  [dbpedia-search-term twitter-search-term twitter-search-count tweet-extract-count]
  (try
    (let [abstract-fvec (do-fvec-dbpedia dbpedia-search-term )]
         ;; need to add GitHub to differentiate results e.g Ruby color vs Ruby programming language
         (let [twitter-search (do-twitter-search (str twitter-search-term " Github") twitter-search-count)]
           (->> twitter-search
                (structure-tweet-data)
                (filter-tweets-by-gh)
                (map #(into %1 (assoc {} :cosim (get-tweet-cosim (:screen_name %1) tweet-extract-count abstract-fvec))))
                (map #(into %1 (assoc {} :boc (github/get-github-user-boc (:screen_name %1) twitter-search-term))))
                )))(catch Exception e (str "Sorry, there was an error: " (.getMessage e)))))


