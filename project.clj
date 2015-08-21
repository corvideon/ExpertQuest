(defproject expertquest "1.0"
  :description "ExpertQuest prototype expert finding system for Twitter and GitHub"
  :url "http://localhost:8081"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [local-file "0.1.0"]
                 [twitter-api "0.7.8"]
                 [clj-http "1.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [tentacles "0.3.0"]
                 [tonyduan/clj-yahoo-boss "1.0.2"]
                 [clojure-opennlp "0.3.3"]
                 [clj-fuzzy "0.1.8"]
                 [biscuit "1.0.0"]
                 ;; web-app classes
                 [hiccup "1.0.5"]
                 [hiccup-bootstrap "0.1.2"]
                 [ring/ring-defaults "0.1.4"]
                 [compojure "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 ]
  :main ^:skip-aot expertquest.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler expertquest.web.handler/app})
