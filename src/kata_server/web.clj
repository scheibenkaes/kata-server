(ns kata-server.web
  (:use [clojure.set :only [difference]])
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

(defn leader-board [matches] 
  "Transform the given matches into the form of:
  {:player-name1 (int times-won) :pl...}"
  (let [players (-> (first matches) :players player-names set)
        winners (map :active-player matches)
        winners-freq (frequencies winners)
        players-not-won-a-round (difference players (set winners))
        players-with-0 (for [p players-not-won-a-round] [p 0])]
    (into winners-freq players-with-0)))

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

(defpartial final-board [matches]
  (let [board (leader-board matches)]
    [:div#board
     [:h2 "Gesamtergebnis"]
     [:table 
      [:tr [:th "Platzierung"] [:th "Name"] [:th "Siege"]]
      (for [[rank nm w] (map-indexed (fn [i [n wins]] [(inc i) n wins]) (sort-by second > board))] [:tr [:td (str rank)] [:td (name nm)] [:td (str w)]])]]))

(defpartial match-table
  [{:keys [sums final-roster] :as match}]
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

(defpartial results
  [matches]
  (final-board matches)
  [:div#results
   (match-tables matches)])

(defpage "/data/dist" []
  (json (distribution-to-chart @last-game-played)))

(defpage "/" []
  (main-layout "Übersicht über die zuletzt gespielten Partien." (results @last-game-played)))
