(ns expertquest.nlp.textanalysis
  "ExpertQuest NLP library to score text context - based on code from github.com/dakrone/clojure-opennlp"
  (:require [opennlp.nlp :as nlp])
  (:require [opennlp.tools.filters :as filters])
  (:require local-file)
  (:require [clojure.string :as string])
  (:require [clj-fuzzy.stemmers :as stemmers])
  (:require [clj-fuzzy.metrics :as metrics])
  (:require [biscuit.core :as digest]))

;; the size of the feature hash vector
(def ^:private fvec-size 256)

(def ^:private get-sentences
  (nlp/make-sentence-detector (local-file/file* "models/en-sent.bin")))

(def ^:private  tokenize
  (nlp/make-tokenizer (local-file/file* "models/en-token.bin")))

(def ^:private  pos-tag
  (nlp/make-pos-tagger (local-file/file* "models/en-pos-maxent.bin")))

;; an atom to hold the feature hash
(def ^:private fvec-atom (atom (into [] (replicate fvec-size 0))))

(defn- clear-fvec-atom
  "Clears the twitter search atom"
  []
  (reset! fvec-atom (into [] (replicate fvec-size 0))))


(defn- nouns-verbs-filter
  "Filter tagged text by nouns and verbs"
  [input-text]
  (->> input-text
      (tokenize)
      (pos-tag)
      (filters/nouns-and-verbs)
      (filter #(>= (count (first %1)) 3))
    )
  )

(defn- porter-stem-text
  "Porter stemmer"
  [input-text]
  (stemmers/porter input-text))

(defn- clean-string
  "Removes punctuation"
  [input-text]
  (if (not= nil input-text)
    (string/replace input-text #"[^\w\s]" " " )))


(defn conj-stemmed-nouns-verbs
  "Conj sentences by nouns and verbs, stemming them at the same time"
  [input-text]
  (->> input-text
       (clean-string)
       (nouns-verbs-filter)
       (map #(porter-stem-text (first %1)))
       (string/join " ")))

(defn dice-coefficient
  "Sorensen / Dice coefficient of two strings"
  [string1 string2]
  (metrics/dice string1 string2))

(defn get-fvec-for-text
  "Transforms a string into a feature hash vector"
  [input-text]
  (clear-fvec-atom)
  (let [stemmed-text (conj-stemmed-nouns-verbs input-text)]
    (let [word-vec (string/split stemmed-text #" ")]
      (doseq [x (range 0 (count word-vec)) :let [word (get word-vec x)]]
        (let [index (mod (digest/crc32 word) fvec-size)]
          (let [old-value (get @fvec-atom  index)]
            (swap! fvec-atom assoc index (inc old-value))))) ;;cumulative count
      (identity @fvec-atom))))

(defn get-fvec-for-text-no-freq
  "Transforms a string into a feature hash vector without tracking word frequency"
  [input-text]
  (clear-fvec-atom)
  (let [stemmed-text (conj-stemmed-nouns-verbs input-text)]
    (let [word-vec (string/split stemmed-text #" ")]
      (doseq [x (range 0 (count word-vec)) :let [word (get word-vec x)]]
        (let [index (mod (digest/crc32 word) 256)]
          (swap! fvec-atom assoc index 1)))                 ;; no cumulative count
      (identity @fvec-atom))))


(defn- mapping-helper [func args]
  "From the source of the incanter core package at https://github.com/incanter"
  (reduce (fn [A B]
            (cond
              (number? A) (func A B)
              (and (coll? A) (coll? (first A)))
              (map (fn [a] (map #(func %1 B) a)) A)
              (coll? A) (map #(func %1 B) A)))
          args))

(defn pow
  "
  (From the source of the incanter core package at https://github.com/incanter)
  This is an element-by-element exponent function, raising the first argument
  by the exponents in the remaining arguments. Equivalent to R's ^ operator.
  "
  [& args]
  (mapping-helper #(Math/pow %1 %2) args))

(defn cosine-similarity
  "(From the source of the incanter core package at https://github.com/incanter/)

  Explained at http://www.appliedsoftwaredesign.com/archives/cosine-similarity-calculator/
  The Cosine Similarity of two vectors is an arbitrary mathematical measure of how similar two vectors are
  on a scale of [0, 1]. 1 being that the vectors are either identical,
  or that their values differ by a constant factor."

  [a b]
  (let [counts (apply merge-with +
                      (map
                        (fn [[x y]]
                          {:dot (* x y)
                           :a (pow x 2)
                           :b (pow y 2)})
                        (map vector a b)))]
    (/ (:dot counts)
       (* (Math/sqrt (:a counts))
          (Math/sqrt (:b counts))))))


