(ns kata-server.test.stats
  (:use clojure.test)
  (:use [kata-server stats]))

(deftest test-last-matches
  (is (= [] @last-matches)))
