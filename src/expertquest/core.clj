(ns expertquest.core
  "ExpertQuest expert finding system"
  (:require [ring.adapter.jetty :as jetty])
  (:require [expertquest.web.handler :as handler])
  (require [expertquest.search.dumpdata :as dump])
  (:gen-class))



(defn -main
  [& args]
  (println "Starting ExpertQuest on http://localhost:8082")
  (jetty/run-jetty  handler/app {:port 8082})
  ;(println "Dumping data...")
  ;(dump/dump-search-data)
  )
