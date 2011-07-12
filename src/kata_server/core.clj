(ns kata-server.core
  (:use clojure.contrib.command-line)
  (:use [kata-server server match socket auth])
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

(defn start-one-match [players] 
  (let [line-up (shuffle players)
        match (new-match line-up)]
    (println line-up)
    (println match)))

(defn player-queue-watcher [_ reference old-state new-state] 
  (when (>= (count new-state) *min-players*)
    (start-one-match new-state)))

(defn -main [& args] 
  (with-command-line
    args
    "Usage java kata-server-*-standalone.jar"
    [[num? n? "On how many connected players should a match start" *min-players*]
     [points? "Points needed to win a match" 50]
     [port? p? "Port to listen to" 8000]]
    (do
      (add-watch players-queue :startup player-queue-watcher)
      (binding [*min-players* num?
                *max-points* points?
                *port* port?]
        (server-loop on-connection-created)))))
