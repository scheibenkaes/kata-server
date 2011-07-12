(ns kata-server.core
  (:use clojure.contrib.command-line)
  (:use kata-server.server)
  (:use kata-server.socket)
  (:use kata-server.auth)
  (:use clojure.java.io)
  (:gen-class))

(def *min-players* 2)

(defn decline-player [sock] 
  (let [out (writer sock)]
    (do
      (.write out "Anmeldung fehlgeschlagen.\n")
      (.flush out)
      (.close sock))))

(def players-queue (ref []))

(defn add-to-queue [player] 
  (do
    (dosync
      (alter players-queue conj player))
    (send-line player (str "HELO " (:name player)))))

(defn on-connection-created [sock] 
  (let [player (authenticate sock)]
    (if player
      (add-to-queue player)
      (decline-player sock))))

(defn -main [& args] 
  (with-command-line
    args
    "Usage java ...-standalone.jar -n number_of_players"
    [[number n "On how many connected players should a match start" *min-players*]]
    (do
      (binding [*min-players* number]
        (server-loop on-connection-created)))))
