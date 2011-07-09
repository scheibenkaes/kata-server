(ns kata-server.socket
  (:use [clojure.java.io :only [reader]]))

(defn read-line [sock] 
  (when (and sock (.isConnected sock))
    (let [in (reader sock)]
      (.readLine in))))
