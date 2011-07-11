(ns kata-server.match
  (:use kata-server.socket))

(defn player-names [players] (map :name players)) 

(defn new-match [players] 
  (let [roster (apply hash-map (interleave (map :name players) (repeat 0))) ]
    {:roster roster
     :players players
     :current-play []
     :active-player (:name (first players))
     }))

(def *max-points* 50)

(defn receive-decision [active-player] 
  (do
    (send-line active-player "DECIDE")
    (let [answer (receive-line active-player)]
      (cond 
        (.startsWith answer "ROLL") :roll
        (.startsWith answer "HOLD") :hold
        :else :error))))

(defn hold [match] 
  )

(defn error [match] 
  )

(defn next-player [cur all] 
  (let [player-cycle (cycle (player-names all))
        next-in-list (drop-while #(not= % cur) player-cycle)]
    (second next-in-list)))

(defn roll [dice match] 
  (if (= 6 dice)
    (assoc match 
           :current-play []
           :active-player (next-player (:active-player match) (:players match)))
    (update-in match [:current-play] conj dice)))

(defn run-match [{:keys [active-player roster] :as match}] 
  (let [dice (inc (rand-int 6))
        decision (receive-decision active-player)]
    (let [new-match 
          (case decision
            :roll (roll dice match)
            :hold (hold match)
            :error (error match))]
      (recur new-match))))
