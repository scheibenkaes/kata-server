(ns kata-server.socket
  (:use [clojure.java.io :only [reader writer]]))

(defn write-line [sock line] 
  (let [out (writer sock)]
    (doto out
      (.write line)
      .newLine
      .flush)))

(defn read-line [sock] 
  (when (and sock (.isConnected sock))
    (let [in (reader sock)]
      (.readLine in))))
