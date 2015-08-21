(ns expertquest.web.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [expertquest.web.views :as views]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
           (GET "/" []  (views/home-page))
           (GET "/search" [] (views/search-page))
           (GET "/search-results" [plang] (views/search-results plang))
           (GET "/about" [] (views/about-page))
           (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))