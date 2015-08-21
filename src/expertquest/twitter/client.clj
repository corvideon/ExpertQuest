(ns expertquest.twitter.client
  "ExpertQuest Twitter client library"
  (:require [expertquest.config.load :as config])
  (:require [twitter.oauth :as oauth])
  (:require [twitter.api.search :as search])
  (:require [twitter.api.restful :as rest]))

;; oauth credentials for twitter / expertquest app
(def ^:private my-creds (oauth/make-oauth-creds
                          (:consumer-key config/twitter-config)
                          (:consumer-secret config/twitter-config)
                          (:access-token config/twitter-config)
                          (:access-token-secret config/twitter-config)))

(defn- create-twitter-search-call
  "Creates a Twitter search API call using OAuth credentials, a search term and a number of tweets to return"
  [search-term search-count]
  (search/search :oauth-creds my-creds
                 :params {
                          :q search-term
                          :count search-count
                          :lang "en"}))

(defn- create-twitter-user-timeline-call
  [screen-name search-count]
  (rest/statuses-user-timeline :oauth-creds my-creds
                               :params {
                                        :screen_name screen-name
                                        :count search-count
                                        :lang "en"}))

(defn- create-tweet-map
  "Creates a map of information on a single tweet"
  [text user]
  (assoc {} :followers (:followers_count user)
            :friends (:friends_count user)
            :text text
            :user (:name user)
            :screen_name (:screen_name user)))

(defn- extract-tweet-data
  "Extracts the data from one tweet"
  [tweet-data-map]
  (let [{text :text user :user} tweet-data-map]
    (create-tweet-map text user)))


(defn search-twitter
  "Searches twitter for <search-term>, returning <search-count> tweets"
  [search-term search-count]
  (map #(into {} %1)
       (map extract-tweet-data
            (:statuses (:body (create-twitter-search-call search-term search-count))))))

(defn get-user-tweet-texts
  "Gets the tweets for user <screen-name>, returning <search-count> tweets"
  [screen-name search-count]
  (map
    #(:text %1)
    (:body (create-twitter-user-timeline-call screen-name search-count))))