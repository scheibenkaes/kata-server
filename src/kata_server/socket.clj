(ns kata-server.socket
  (:use [clojure.java.io :only [reader writer]]))

(defn has-a-socket-in-meta? [obj & _]
  (-> obj meta :socket nil? not))

(defmulti receive-line has-a-socket-in-meta?)

(defmethod receive-line true
  [obj]
  (receive-line (:socket (meta obj))))

(defmethod receive-line false [sock] 
  (when (and sock (.isConnected sock))
    (let [in (reader sock)]
      (.readLine in))))

(defmulti send-line has-a-socket-in-meta?)

(defmethod send-line false [sock line]
  (when (and sock (.isConnected sock))
    (let [out (writer sock)]
      (doto out
        (.write line)
        .newLine
        .flush))))

(defmethod send-line true [player line]
  (send-line (-> player meta :socket) line))

(defn multicast-line [recipients line] 
  (doseq [rec recipients]
    (send-line (-> rec meta :socket) line)))
