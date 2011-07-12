(ns kata-server.match
  (:use kata-server.socket))

(def *max-points* 50)

(def *rounds-to-play* 10)

(defn player-names [players] (map :name players)) 

(defn new-match [players] 
  (let [roster (apply hash-map (interleave (map :name players) (repeat []))) ]
    {:roster roster
     :players players
     :current-play []
     :active-player (:name (first players))}))

(defn receive-decision [active-player] 
  (do
    (send-line active-player "DECIDE")
    (let [answer (receive-line active-player)]
      (cond 
        (nil? answer) :error
        (.startsWith answer "ROLL") :roll
        (.startsWith answer "HOLD") :hold
        :else :error))))

(defn next-player 
  ([cur all] 
   (let [player-cycle (cycle (player-names all))
         next-in-list (drop-while #(not= % cur) player-cycle)]
     (second next-in-list)))
  ([match]
   (next-player (:active-player match) (:players match))))

(defn hold [{:keys [roster active-player current-play] :as match}] 
  (let [next-p (next-player match)]
    (assoc match
           :active-player next-p
           :current-play []
           :roster (update-in roster [active-player] conj current-play))))

(defn id->obj [id {players :players}] 
  (first (filter #(= id (:name %)) players)))

(defn error [{:keys [active-player] :as match}] 
  (do
    (send-line (id->obj active-player match) "ERROR: Unkown command.")
    (assoc match :active-player (next-player match) :current-play [])))

(defn update-match-on-roll [dice match] 
  (if (= 6 dice)
    (assoc match 
           :current-play []
           :active-player (next-player (:active-player match) (:players match)))
    (update-in match [:current-play] conj dice)))

(defn roll [dice {:keys [active-player players] :as match}] 
  (let [new-match-state (update-match-on-roll dice match)
        throw-msg (str "THROW " (name active-player) " threw a " dice)]
    (do 
      (multicast-line players throw-msg)
      new-match-state)))

(defn sum-roster
  ([player {roster :roster}]
   (let [players-roster (player roster)]
     (apply + (flatten players-roster))))
  ([match] (sum-roster (:active-player match) match)))

(defn sum-current-play [match] 
  (apply + (:current-play match)))

(defn pprint-roster [roster] 
  (butlast (interleave (map (fn [[k v]] (str (name k) ": " v)) (sort-by second > roster)) (repeat ", "))))

(defn roster-with-sums [{:keys [roster] :as match}] 
  (into {} (map (fn [[k _]] [k (sum-roster k match)]) roster)))

(defn send-current-roster-to-all [{:keys [roster players] :as match}] 
  (multicast-line players (apply str "ROSTER: " (pprint-roster (roster-with-sums match)))))

(defn sum-of-active-player [match] 
  (+ (sum-roster match) (sum-current-play match)))

(defn calc-final-result [{:keys [active-player roster] :as match}] 
  (let [result (roster-with-sums match)]
    (update-in result [active-player] + (sum-current-play match))))

(defn end-of-match [match] 
  (let [result (calc-final-result match)
        pretty (pprint-roster result)
        presult (apply str "RESULT: " pretty)]
    (multicast-line (:players match) presult)))

(defn run-match [{:keys [active-player roster] :as match}] 
  (let [someone-won? (>= (sum-of-active-player match) *max-points*) ]
    (if someone-won?
      (end-of-match match)
      (let [active-player-obj (id->obj active-player match)
            decision (receive-decision active-player-obj)
            new-match-state (case decision
                        :roll (roll (inc (rand-int 6)) match)
                        :hold (hold match)
                        :error (error match))]
        (recur new-match-state)))))

