(ns expertquest.dbpedia.client
  "ExpertQuest DBPedia client library"
  (:require [clj-http.client :as http-client])
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as string])
  )

;; The URI used for abstracts in DBPedia JSON
(def ^:private abstract-uri "http://dbpedia.org/ontology/abstract")

;; The URI used for disambiguation in DBPedia JSON
(def ^:private disambiguation-uri "http://dbpedia.org/ontology/wikiPageDisambiguates")

;; A var to hold the partial URI prefix for DBPedia
(def ^:private dbpedia-uri "http://dbpedia.org/resource/")

;; A var to hold the partial suffix for disambiguation
(def ^:private disambiguation-suffix "_(disambiguation)")

;; A var to hold the lang to search for - currently "en"
(def ^:private requested-lang "en")

(defn- create-dbpedia-http-call
  "Create a http call to DBPedia"
  [resource-uri]
  (http-client/get resource-uri {:accept :json}))

(defn- get-body-as-json
  "Gets the body of the DBPedia return as a json string"
  [resource-uri]
  (get
    (json/read-str
      (:body (create-dbpedia-http-call resource-uri))) resource-uri))

(defn- is-requested-lang?
  "Tests if the language is the requested one"
  [lang]
  (or (= (get lang "lang") requested-lang) (= (get lang "lang") nil)))

(defn- capitalize-words
  "Capitalize every word in a string"
  [s]
  (->> (string/split (str s) #"\b")
       (map string/capitalize)
       (string/join)))

(defn- get-resource-uri-string
  "Formats the resource-uri string correctly"
  [resource-name]
  (if (not= nil resource-name)
    (-> resource-name
        (string/replace " " "_")
        )))

(defn- get-reverse-resource-uri-string
  "Formats the returned resource string correctly"
  [resource-name]
  (-> resource-name
      (string/split #"/")
      (string/replace " " "_")
      ))

(defn- get-dbpedia-disambiguation-uris
  "Gets the DBPedia disambiguation uris for <resource-name> if available"
  [resource-name]
  (let [resource-uri (str dbpedia-uri (get-resource-uri-string resource-name) disambiguation-suffix)]
    (let [disambiguations []]
      (conj disambiguations
            (map #(get %1 "value")
                 (get (get-body-as-json resource-uri) disambiguation-uri))))))

(defn get-dbpedia-disambiguations
  "Gets the DBPedia disambiguation uris for <resource-name> if available"
  [resource-name]
  (let [disambiguation-uris (get-dbpedia-disambiguation-uris resource-name)]
    (map
      #(get-reverse-resource-uri-string %1)
      (first disambiguation-uris))))


(defn get-dbpedia-abstract
  "Gets a DBPedia abstract for <resource-name> if available"
  [resource-name]
  (let [resource-uri (str dbpedia-uri (get-resource-uri-string resource-name))]
    (get (first
           (filter #(is-requested-lang? %1)
                   (get (get-body-as-json resource-uri) abstract-uri))) "value")))

