(ns kata-server.socket
  (:use [clojure.java.io :only [reader writer]]))

(defn receive-line [sock] 
  (when (and sock (.isConnected sock))
    (let [in (reader sock)]
      (.readLine in))))

(defn send-line [sock line] 
  (let [out (writer sock)]
    (doto out
      (.write line)
      .newLine
      .flush)))

(defn read-line-with-timeout [sock timeout] 
  (let [a (agent nil)
        f (fn [_] (receive-line sock))]
    (do
      (send-off a f)
      (when (await-for timeout a) @a))))

