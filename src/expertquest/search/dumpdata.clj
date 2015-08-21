(ns expertquest.search.dumpdata
  "Dumps all search data to the command line"
  (:require [expertquest.search.core :as search])
  (:require [expertquest.github.client :as github]))

(defn do-search-for-dump
  "Modified search algorithm"
  [dbpedia-search-term twitter-search-term twitter-search-count tweet-extract-count]
  (try
    (let [abstract-fvec (search/do-fvec-dbpedia dbpedia-search-term )]
      (let [twitter-search (search/do-twitter-search (str twitter-search-term " Github") twitter-search-count)]
        (->> twitter-search
             (search/structure-tweet-data)
             (search/filter-tweets-by-gh)
             (map #(into %1 (assoc {} :cosim (search/get-tweet-cosim (:screen_name %1) tweet-extract-count abstract-fvec))))
             (map #(into %1 (assoc {} :boc (github/get-github-user-boc (:screen_name %1) twitter-search-term))))
             (map #(into %1 (assoc {} :plang twitter-search-term)))
             ;(println)
             ;(map #(println %1))
             )))(catch Exception e (str "Sorry, there was an error: " (.getMessage e)))))


(defn dump-search-data
  "dumps all data for the tiobe top 50"
  []
  (let [tiobe-terms (into (sorted-map) (search/get-tiobe-terms))]
    (println "Starting... ")
    (map #(let [plang (key %1)]
           (let [
                 t1 (identity search/twitter-search-count)
                 t2 (identity search/user-timeline-count)]
             (println (do-search-for-dump (get (search/get-tiobe-terms) plang) plang t1 t2))
             )
           ) tiobe-terms)
    ))

