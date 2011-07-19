(ns kata-server.test.stats
  (:use clojure.test)
  (:use [kata-server stats]))

(def test-data 
  {:roster {:foo [[1 2 3] [4 5]] :bar [[3 2 1]]} :current-play [5 5 5] :active-player :bar})

(def test-game
  [test-data test-data])

(deftest test-add-final-roster
  (is (= {:foo [[1 2 3] [4 5]] :bar [[3 2 1] [5 5 5]]}
         (:final-roster (add-final-roster test-data)))))

(deftest test-add-sums
  (is (= 15 (-> (add-final-roster test-data) add-sums :sums :foo)))
  (is (= 21 (-> (add-final-roster test-data) add-sums :sums :bar))))

(deftest test-save-stats
  (do
    (dosync (ref-set last-game-played []))
    (save-stats [test-data])
    (let [m (first @last-game-played)]
      (is (contains? m :final-roster))
      (is (contains? m :sums))
      (is (contains? m :throw-distribution)))))

(deftest test-add-throw-distri
  (let [d (add-final-roster test-data)]
    (is (= {:foo {1 1 2 1 3 1 4 1 5 1 6 0} :bar {1 1 2 1 3 1 4 0 5 3 6 0}}
           (-> (add-throw-distribution d) :throw-distribution)))))

(deftest test-player-distri 
  (is (= [1 1 2 1 3 1 4 1 5 1 6 0]
         (player-distri (-> test-data :roster :foo)))))
