(ns kata-server.test.player
  (:use clojure.test)
  (:use kata-server.player))

(deftest test-new-player 
  (is (keyword? (:name (new-player "asd")))))
