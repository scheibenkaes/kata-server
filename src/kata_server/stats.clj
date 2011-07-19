(ns kata-server.stats
  (:use [clojure.contrib.json :only [json-str]]))

(def last-game-played (ref nil))

(defn valid-throw? [n] (not= 6 n))

(defn add-sums [match] 
  "Add a map of player name to points to the given match."
  (assert (contains? match :final-roster))
  (assoc match :sums (into {} (for [[k v] (:final-roster match)] [k (->> v flatten flatten (apply +))]))))

(defn matches->json [] 
  (json-str @last-game-played))

(defn add-final-roster [{:keys [roster current-play active-player] :as match}] 
  "Add the final roster under the :final-roster keyword to the given match."
  (assoc match :final-roster (update-in roster [active-player] conj current-play)))

(defn player-distri [roster]
  (let [flat (flatten roster) occ (for [dice (range 1 7)] [dice (count (filter #(= dice %) flat))])]
    (flatten occ)))

(defn add-throw-distribution [{:keys [final-roster] :as match}]
  "Adds the distribution of the thrown numbers for each player
  under :throw-distribution"
  (assoc match
         :throw-distribution 
         (into {} (for [[k v] final-roster] [k (apply hash-map (player-distri (k final-roster)))]))))


(defn process-matches [matches] 
  (dosync
    (ref-set 
      last-game-played 
      (->> (map add-final-roster matches) (map add-sums) (map add-throw-distribution)))))
