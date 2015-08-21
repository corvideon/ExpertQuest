(ns expertquest.web.views
  "ExpertQuest web page definitions"
  (:require [clojure.string :as string]
            [expertquest.search.core :as search]
            [ring.util.response :as response]
            [hiccup.page :as page]
            ))

(defn gen-page-head
  "HTML <head>"
  [title]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (page/include-css "bootstrap/css/bootstrap.min.css")
   (page/include-css "bootstrap/css/bootstrap-theme.min.css")
   (page/include-css "bootstrap/css/spinner.css")
   (page/include-js "bootstrap/js/jquery-1.11.2.min.js")
   (page/include-js "bootstrap/js/bootstrap.min.js")
   (page/include-js "bootstrap/js/spin.min.js")
   (page/include-js "bootstrap/js/spinner.js")
   [:title (str "ExpertQuest: " title)]])

(def page-title [:h1 "ExpertQuest " [:small "Helping you find coding experts on the web"] ])

(def navbar [:span [:a {:href "/"} "Home"] " | " [:a {:href "/search"} "Search for Programming Experts"] " | " [:a {:href "/about"} "About"]])

(def hr [:hr])

(def footer "&copy; Corvideon 2015")

(defn home-page
  []
  (page/html5
    (gen-page-head "Home")
    [:body {:role "document"}
     [:div {:class "container"}
      page-title
      navbar
      hr
      [:h3 "Welcome to ExpertQuest"]
      [:p "ExpertQuest will search Twitter and Github for experts in any of the Tiobe Top 50 programming languages."]
      [:ul
       [:li [:a {:href "search"} "Search with ExpertQuest"]]
       [:li [:a {:href "http://www.tiobe.com/index.php/content/paperinfo/tpci/index.html"} "Tiobe Top 50"]]
       ]
      hr
      footer]]))

(defn search-page
  []
  (page/html5
    (gen-page-head "Search")
    [:body {:role "document"}
     [:div {:class "container"}
      page-title
      navbar
      hr
      [:p "Search for coding experts in the Tiobe Top 50+ programming languages..."]
      [:form {:action "search-results" :method "get" :id "searchform"}
       [:div {:class "form-group"}
        [:label {:for "topic"} "Search for experts on "]
        "&nbsp;"
        [:select {:name "plang"}
         (let [tiobe-terms (into (sorted-map) (search/get-tiobe-terms))]
           (map #(let [tiobe-term (key %1)]
                  [:option {:value tiobe-term} tiobe-term  ]
                  ) tiobe-terms)
           )
         ]
        "&nbsp;"
        [:input {:type "button" :name "btn" :value "Search" :id "submitBtn" :data-toggle "modal" :data-target "#confirm-submit" :class="btn btn-default"} ]
        ]
       ]
      [:div {:class "modal fade" :id "confirm-submit" :tabindex "-1" :role "dialog" :aria-labelledby "myModalLabel" :aria-hidden "true"}
       [:div {:class "modal-dialog"}
        [:div {:class "modal-content"}
         [:div {:class "modal-header"} "Confirm ExpertQuest Search"]
         [:div {:class "modal-body"} "This search may take several minutes. OK to continue?"]
         [:div {:class "modal-footer"}
          [:a {:href "#" :id "submit" :class "btn btn-success success"} "OK"]
          [:button {:type "button" :class "btn btn-default" :data-dismiss "modal"} "Cancel"]
          ]]]]
      [:div {:class "modal fade" :id "spinner-modal" :tabindex "-1" :role "dialog" :aria-labelledby "myModalLabel2" :aria-hidden "true"}
       [:div {:class "modal-dialog"}
        [:div {:class "modal-content"}
         [:div {:class "modal-header"} "ExpertQuest is searching..."]
         [:div {:class "modal-body"} "Please wait while your search is processing."]
         [:div {:class "modal-footer"}
          [:div {:class "spinner"}]
          ]]]]
      [:script
      "$('#submit').click(function(){
           $('#spinner-modal').modal('show');
           $('#searchform').submit();
       });"
       ]
      hr
      footer]
    ])
  )


(defn search-results
  [plang]
  (page/html5
    (let [
          t1 (identity search/twitter-search-count)
          t2 (identity search/user-timeline-count)
          search-data (reverse  (sort-by (juxt :boc  :github-followers :cosim :twitter-followers)
                                         (search/do-search
                                           (get (search/get-tiobe-terms) plang) plang t1 t2)))]
      (page/html5
        (gen-page-head "Search")
        [:body {:role "document"}
         [:div {:class "container"}
          page-title
          navbar
          hr
          [:p "ExpertQuest found " (count search-data) " expert candidates for " plang "." ]
          [:table {:class "table table-striped"}
           [:tr
            [:th "Name"] [:th "Accounts"] [:th "Twitter Mentions"] [:th "Bytes of Code"] [:th "GitHub Followers"] [:th "Twitter Followers"] ]
           (for [ i (range 0 (count search-data))]
             (let [ expert-data (nth search-data i)]
               (println expert-data)
               [:tr
                [:td (:user expert-data)]
                [:td
                 [:a  {:href (str "http://www.twitter.com/" (:screen_name expert-data)) :target "_blank"} "http://www.twitter.com/" (:screen_name expert-data)]
                 [:br]
                 [:a  {:href (str "http://www.github.com/" (:screen_name expert-data)) :target "_blank"} "http://www.github.com/" (:screen_name expert-data)]
                 ]
                [:td
                 ;(:cosim expert-data)
                 "&nbsp;" [:div {:class "progress"}
                  [:div {:class "progress-bar" :role "progressbar" :aria-valuenow "100" :aria-valuemin "0" :aria-valuemax "100"
                         :style (str "width:" (* (:cosim expert-data) 100) "%;")}]]
                 ]
                [:td (:boc expert-data)]
                [:td (:github-followers expert-data)]
                [:td (:twitter-followers expert-data)]]
               )
             )
           ]
          hr
          footer]]))))


(defn about-page
  []
  (page/html5
    (gen-page-head "About")
    [:body {:role "document"}
     [:div {:class "container"}
      page-title
      navbar
      hr
      [:h3 "About ExpertQuest"]
      [:p "ExpertQuest is a project by Seamus Brady for the MSc ASE, UCD."]
      [:p "Email seamus (at) corvideon.ie for more information."]
      hr
      footer]]))

