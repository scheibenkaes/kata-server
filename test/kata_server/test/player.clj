(ns kata-server.test.player
  (:use clojure.test)
  (:use kata-server.player))

(deftest test-legal-name?
  (are [legal name] (= legal (legal-name? name))
       true "asdASDF"
       false "asd4"
       false "_-aaaa"
       false ""
       false "aa"
       false "aaaaaaaaa"
       false nil))
