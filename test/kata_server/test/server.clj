(ns kata-server.test.server
  (:use kata-server.server)
  (:use clojure.test))

(deftest create-server-socket-test 
  (with-open [serv-sock (create-server-socket)]
    (is (not (nil? serv-sock)))))
