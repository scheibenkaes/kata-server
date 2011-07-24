(ns kata-server.web
  (:use kata-server.stats)
  (:use [kata-server.match :only [player-names]])
  (:use [noir core response])
  (:use [hiccup core page-helpers]))

(defn add-vecs [& vs] 
  (assert (apply = (map count vs)))
  (if (= 1 (count vs))
    (first vs)
    (vec (map #(apply + %) (partition (count vs) (apply interleave vs))))))

(defn frequencies-or-0 
  ([acc freqs]
   (if (= 6 (count freqs))
     freqs
     (recur (inc acc) (if-let [c (freqs acc)] freqs (assoc freqs acc 0)))))
  ([freqs]
   (frequencies-or-0 1 freqs)))

(defn save-interleave [ps] 
  (if (= 1 (count ps))
    (first ps)
    (apply interleave ps)))

(defn distribution-to-chart [matches] 
  "Take all :throw-distribution values an transform them to
  be rendered to a chart."
  (let [players (-> matches first :players)
        names (-> players player-names sort vec)
        count-players (count names)
        rosters (map :final-roster matches)
        names-to-dist (vec (for [n names] (apply add-vecs (for [ros rosters] (->> (n ros) flatten frequencies frequencies-or-0 (sort-by first) vals vec)))))]
    {:ticks names :data (vec (map vec (->> (save-interleave names-to-dist) (partition count-players))))}))

(defpartial main-layout [title & body]
  (html5 
    [:head
     [:title title]
     (include-js "/js/jquery-1.6.2.min.js")
     (include-js "/js/jquery.jqplot.js")
     (include-js "/js/plugins/jqplot.barRenderer.min.js")
     (include-js "/js/plugins/jqplot.categoryAxisRenderer.min.js")
     (include-js "/js/plugins/jqplot.pointLabels.min.js")
     (include-js "/js/chart.js")
     ]
    [:body body]))

(defpartial match-table
  [{:keys [sums final-roster] :as match}]
  [:h1 "Wurfverteilung"]
  [:div#chart ""]
  [:hr]
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

(defpage "/data/dist" []
  (json (distribution-to-chart @last-game-played)))

(defpage "/" []
  (main-layout "Übersicht über die zuletzt gespielten Partien." (results @last-game-played)))
