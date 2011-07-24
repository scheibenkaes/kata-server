(ns kata-server.test.web
  (:use clojure.test)
  (:use kata-server.web))

(def test-data-single-player
  [{:players [{:name :foo}]
    :final-roster {:foo [[1 3] [3 4 4 4 5] [5 6]]}
    :throw-distribution {:foo {1 1 2 0 3 2 4 3 5 2 6 1} 
                         }}
   ])

(def test-data 
  [{:players [{:name :foo} {:name :bar}]
    :final-roster {:foo [[1 3] [3 4 4 4 5] [5 6]] :bar [[2 2 2 2 2] [3 3 3] [4] (-> (repeat 10 5) vec)]}
    :throw-distribution {:foo {1 1 2 0 3 2 4 3 5 2 6 1} 
                         :bar {1 0 2 5 3 3 4 1 5 10 6 0}}}
   {:players [{:name :foo} {:name :bar}]
    :final-roster {:foo [[1 3] [3 4 4 4 5] [5 6]] :bar [[2 2 2 2 2] [3 3 3] [4] (-> (repeat 10 5) vec)]}
    :throw-distribution {:foo {1 1 2 0 3 2 4 3 5 2 6 1} 
                         :bar {1 0 2 5 3 3 4 1 5 10 6 0}}}])

(def runtime-data '({:throw-distribution {:asd {1 0, 2 0, 3 0, 4 1, 5 2, 6 0}}, :sums {:asd 14}, :final-roster {:asd [[5] [4 5]]}, :roster {:asd [[5]]}, :players [{:name :asd}], :current-play [4 5], :active-player :asd, :max-points 10}
                    {:throw-distribution {:asd {1 0, 2 0, 3 0, 4 1, 5 2, 6 0}}, :sums {:asd 14}, :final-roster {:asd [[5] [4 5]]}, :roster {:asd [[5]]}, :players [{:name :asd}], :current-play [4 5], :active-player :asd, :max-points 10}))

(deftest test-runtime-data 
  (let [exp {:ticks [:bar :foo] :data [[0 2] [10 0] [6 4] [2 6] [20 4] [0 2]]}] 
    (is (= exp (distribution-to-chart test-data)))))
; ticks: [Player1 Player2 Player3]
; data: [1er-sp1 2er-sp2 ...]
; data: [1er-sp2 2er-sp2 ...]
; ...
; => {:ticks [Player1 ...] :data {Player1 [[...]]}}

(deftest test-save-interleave 
  (is (= [1 2 3 4 5 ] (save-interleave [[1 2 3 4 5]]))))

(deftest test-single-player-data 
  (let [exp {:ticks [:foo] :data (vec (partition 1 [2 0 4 6 4 2]))}]
    (is (= exp (distribution-to-chart test-data-single-player)))))

(deftest test-distribution-to-chart-data
  (let [result (distribution-to-chart test-data)]
    (is (= {:ticks [:bar :foo] :data {:foo [2 0 4 6 4 2] :bar [0 10 6 2 20 0]}}
           result))))

(deftest test-add-vecs
  (let [v1 [1 2 3 4 5]
        v2 [2 4 6 8 10]]
    (is (= [3 6 9 12 15]
           (add-vecs v1 v2)))))
