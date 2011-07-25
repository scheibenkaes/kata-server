(ns kata-server.core
  (:use [kata-server server match socket auth web stats])
  (:require [noir.server :as noir])
  (:use clojure.java.io 
        clojure.contrib.command-line)
  (:gen-class))

(def *rounds-to-play* (atom 1))

(def *min-players* (atom 2))

(def *max-points* (atom 50))

(defn decline-player [sock] 
  (let [out (writer sock)]
    (do
      (.write out "Anmeldung fehlgeschlagen.\n")
      (.flush out)
      (.close sock))))

(def players-queue (ref []))

(defn add-to-queue [player] 
  (do
    (send-line player (str "HELO " (-> player :name name) (format " [ROUNDS %s, MAX %s]" @*rounds-to-play* @*max-points*)))
    (dosync
      (alter players-queue conj player))))

(defn on-connection-created [sock] 
  (let [player (authenticate sock)]
    (if player
      (add-to-queue player)
      (decline-player sock))))

(defn close-connection [players] 
  (doseq [player players]
    (-> player meta :socket (doto .close))))

(defn run-single-match [match] 
  "Run a single match and return its result."
  (do
    (send-current-roster-to-all match)
    (run-match match)))

(defn start-matches [n players] 
  (let [matches (for [_ (range n)] (new-match (shuffle players) :max-points @*max-points*))]
    (do
      (let [results (doall (map run-single-match matches))]
        (save-stats results))
      (close-connection players))))

(defn player-queue-watcher [_ reference old-state new-state] 
  (when (>= (count new-state) @*min-players*)
    (do 
      (start-matches @*rounds-to-play* new-state)
      (dosync (ref-set players-queue [])))))

(defn run-queue [] 
  (let [f (partial server-loop on-connection-created)
        t (Thread. f)]
    (doto t .start)))

(defn -main [& args] 
  (with-command-line
    args
    "Usage java kata-server-*-standalone.jar"
    [[number n "On how many connected players should a match start" "2"]
     [points "Points needed to win a match" "50"]
     [port p "Port to listen to" "8000"]
     [web? w? "Start the web interface" false]
     [matches m "Number of matches to play" "1"]]
    (do
      (System/setProperty "java.util.logging.config.file" "logging.properties")
      (add-watch players-queue :startup player-queue-watcher)
      (reset! *min-players* (Integer/parseInt number))
      (reset! *max-points* (Integer/parseInt points))
      (reset! *rounds-to-play* (Integer/parseInt matches))
      (when web? (noir/start 8001 {}))
      (server-loop on-connection-created :port (Integer/parseInt port)))))
