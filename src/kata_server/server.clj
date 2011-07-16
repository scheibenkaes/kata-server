(ns kata-server.server
  (:require [clojure.contrib.logging :as log])
  (:import [java.net ServerSocket]))

(defn create-server-socket [port] 
  (do
    (log/info (str "Starting server on port " port))
    (ServerSocket. port)))

(defn server-loop [callback & args] 
  (let [opts (apply hash-map args)]
    (with-open [serv-sock (create-server-socket (:port opts))]
      (loop []
        (let [sock (.accept serv-sock)
              thread (Thread. (partial callback sock))]
          (do
            (log/debug (str "Incoming connection from " sock))
            (.start thread)
            (recur)))))))
