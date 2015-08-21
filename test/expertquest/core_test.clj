(ns expertquest.core-test
  (:require [clojure.test :refer :all])
  (:require [expertquest.twitter.client :as twitter-client])
  (:require [expertquest.dbpedia.client :as dbpedia-client])
  (:require [expertquest.github.client :as github-client])
  (:require [expertquest.nlp.textanalysis :as nlp]))

(deftest twitter-search-count-works
  (testing "Check that the count of tweet returned is correct"
    (let [c (count (twitter-client/search-twitter "clojure" 1))]
      (is (= 1 c)))))


(deftest twitter-user-timeline-count-works
  (testing "Check that the count of tweets returned for a user is correct"
    (let [c (count (twitter-client/get-user-tweet-texts "richhickey" 5))]
      (is (= 5 c)))))


(deftest github-user-works
  (testing "Check that we can load users from GitHub"
    (let [user (:name (github-client/get-github-user-data "richhickey"))]
      (is (= "Rich Hickey" user)))))


(deftest dbpedia-abstract-works
  (testing "Check that we can load an abstract from DBPedia"
    (let [abstract (dbpedia-client/get-dbpedia-abstract "Clojure")]
      (is (.contains abstract "Clojure")))))


(deftest noun-verb-extract-works
  (testing "Check that NLP libraries are loaded and working as expected"
    (let [stemmed-text (nlp/conj-stemmed-nouns-verbs "humpty dumpty!! sat on a wall??")]
      (is (= stemmed-text "humpti sat wall")))))


(deftest dice-coefficient-works
  (testing "Check that NLP dice coefficient is returned as expected"
    (let [dice (nlp/dice-coefficient "healed" "health")]
      (is (= dice 0.6)))))

;; This is the DBPedia abstract on Clojure, produced in repl by (dbpedia-client/get-dbpedia-abstract "Clojure")
(def abstract "Clojure (pronounced like \"closure\") is a dialect of the Lisp programming language created by Rich Hickey.
Clojure is a functional general-purpose language, and runs on the Java Virtual Machine, Common Language Runtime, and JavaScript engines.
 Like other Lisps, Clojure treats code as data and has a sophisticated macro system.Clojure's focus on programming with immutable values
 and explicit progression-of-time constructs are intended to facilitate the development of more robust programs, particularly multithreaded ones.")

(deftest fvec-on-abstract-works
  (testing "Check that the feature hash vector is returned as expected on a DBPedia abstract"
    (let [fvec (nlp/get-fvec-for-text abstract)]
      (is (= (apply + fvec) 42)))))                         ;; yes, Clojure is the answer to Life, the Universe and Everything