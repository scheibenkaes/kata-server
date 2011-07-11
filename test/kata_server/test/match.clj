(ns kata-server.test.match
  (:use kata-server.match)
  (:use clojure.test))

(def test-match (new-match [{:name :foo} {:name :bar}]))

(deftest test-player-names 
  (is (= [:foo :bar] (player-names (:players test-match)))))

(deftest test-new-match 
  (let [match test-match]
    (is (contains? match :roster))
    (is (contains? match :current-play))
    (is (contains? match :active-player))
    (is (contains? match :players))))

(deftest test-roll-with-a-number-other-than-6 
  (let [dice 5
        {:keys [current-play roster active-player]} (roll dice (update-in test-match [:current-play] conj 5))]
    (is (= current-play [5 5]))
    (is (= roster (:roster test-match)))
    (is (= active-player :foo))))

(deftest test-roll-with-6 
  (let [in-game-match (update-in test-match [:current-play] conj 4)
        {:keys [current-play active-player roster]} (roll 6 in-game-match)]
    (is (= :bar active-player))
    (is (= (:roster in-game-match) roster))
    (is (= [] current-play))))

(deftest test-hold 
  (let [rost [3 2 1 5]
        {:keys [active-player current-play roster]} (hold (assoc test-match :roster {:foo [[2 3 5] [5 5]]} :current-play rost))]
    (is (= [[2 3 5] [5 5] [3 2 1 5]] (:foo roster)))
    (is (= :bar active-player))
    (is (= [] current-play))))

(deftest test-sum-roster 
  (let [roster [[2 3 4] [3 5 1] [3]]
        match (update-in test-match [:roster :foo] cons roster)
        sum (sum-roster match)
        sum2 (sum-roster (:active-player match) match)]
    (is (= sum sum2))
    (is (= 21 sum))))

(deftest test-sum-current-play 
  (let [play [3 4 5 1]
        match (assoc test-match :current-play play)]
    (is (= 13 (sum-current-play match)))))

