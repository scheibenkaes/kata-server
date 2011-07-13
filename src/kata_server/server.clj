(ns kata-server.server
  (:require [clojure.contrib.logging :as log])
  (:import [java.net ServerSocket]))

(def *port* 8000)

(defn create-server-socket [] 
  (do
    (log/info (str "Starting server on port " *port*))
    (ServerSocket. *port*)))

(defn server-loop [callback] 
  (with-open [serv-sock (create-server-socket)]
    (loop []
      (let [sock (.accept serv-sock)
            thread (Thread. (partial callback sock))]
        (do
          (log/debug (str "Incoming connection from " sock))
          (.start thread)
          (recur))))))
