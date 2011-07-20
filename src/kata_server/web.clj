(ns kata-server.web
  (:use kata-server.stats)
  (:use [kata-server.match :only [player-names]])
  (:use [noir core])
  (:use [hiccup core page-helpers]))

(defn add-vecs [& vs] 
  (assert (apply = (map count vs)))
  (vec (map #(apply + %) (partition (count vs) (apply interleave vs)))))

(defn distribution-to-chart [matches] 
  "Take all :throw-distribution values an transform them to
  be rendered to a chart."
  (let [players (-> matches first :players)
        player-names (-> players player-names vec)
        distris (map :throw-distribution matches)
        init (into {} (for [p player-names] [p (vec (repeat 6 0))]))
        data (reduce 
               (fn [acc cur] 
                 (let [vs (for [n player-names ds (map n distris)]
                            [n [ds]])]
                   (println distris))
                 acc) 
               init 
               distris)]
    {
     :ticks player-names
     :data data
     }))

(defpartial main-layout [title & body]
  (html5 
    [:head
     [:title title]
     (include-js "/js/jquery-1.6.2.min.js")
     (include-js "/js/highcharts.js")
     (include-js "/js/distribution.js")]
    [:body body]))

(defpartial match-table
  [{:keys [sums final-roster] :as match}]
  [:table
   [:tr
    [:th "Platzierung"] [:th "Name"] [:th "Punkte"] [:th "Würfe"]]
   (for [[i [k v]] (map-indexed (fn [cnt [k v]] [(inc cnt) [k v]]) (sort-by second > sums))] [:tr [:td (str i)] [:td (name k)] [:td (str v)] [:td (str (k final-roster))]])])

(defpartial match-tables
  [matches]
  [:h2 "Alle Durchgänge"]
  [:div#match-tables (for [m matches] [:div (match-table m)])]
  [:hr])

(defpartial distribution [{:keys [throw-distribution] :as match}]
  [:div (str match)]
  #_[:div#container.highcharts-container {:style "width: 100%; height: 600px; background: blue;"} ""]
  [:div (match-table match)])

(defpartial results
  [matches]
  [:div#results
   (match-tables matches)])

(defpage "/" []
  (main-layout "Übersicht über die zuletzt gespielten Partien." (results @last-game-played)))
