(ns kata-server.web
  (:use kata-server.stats)
  (:use [noir core])
  (:use [hiccup core page-helpers]))

(defpartial main-layout [title & body]
  (html5 
    [:head
     [:title title]
     (include-js "/js/jquery-1.6.2.min.js")
     (include-js "/js/highcharts.js")]
    [:body body]))

(defpartial distribution [{:keys [throw-distribution] :as match}]
  [:div (str throw-distribution)])

(defpage "/" []
  (main-layout "Render" (distribution (-> @last-game-played first))))
