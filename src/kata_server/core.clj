(ns kata-server.core
  (:use kata-server.server)
  (:use kata-server.socket)
  (:use kata-server.auth)
  (:use clojure.java.io)
  (:gen-class))

(defn decline-player [sock] 
  (let [out (writer sock)]
    (do
      (.write out "Anmeldung fehlgeschlagen.\n")
      (.flush out)
      (.close sock))))

(defn add-to-match [player] 
  (write-line (:socket (meta player)) "HELO "))

(defn on-connection-created [sock] 
  (let [player (authenticate sock)]
    (if player
      (add-to-match player)
      (decline-player sock))))

(defn -main [& args] 
  (server-loop on-connection-created))
