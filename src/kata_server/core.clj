(ns kata-server.core
  (:use [kata-server server match socket auth web stats])
  (:require [noir.server :as noir])
  (:use clojure.java.io 
        clojure.contrib.command-line)
  (:gen-class))

(def *rounds-to-play* (atom 1))

(def *min-players* (atom 2))

(def *max-points* (atom 50))

(def *timeout* (atom 0))

(defn decline-player [sock] 
  (let [out (writer sock)]
    (do
      (.write out "Anmeldung fehlgeschlagen.\n")
      (.flush out)
      (.close sock))))

(def players-queue (ref []))

(defn add-to-queue [player] 
  (do
    (-> player meta :socket (doto (.setSoTimeout @*timeout*)))
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
     [matches m "Number of matches to play" "1"]
     [points "Points needed to win a match" "50"]
     [port p "Port to listen to" "8000"]
     [timeout t "The time (in ms) in which a client has to respond. 0 means no timeout." "0"]
     [web? w? "Start the web interface" false]]
    (do
      (System/setProperty "java.util.logging.config.file" "logging.properties")
      (add-watch players-queue :startup player-queue-watcher)
      (reset! *min-players* (Integer/parseInt number))
      (reset! *max-points* (Integer/parseInt points))
      (reset! *rounds-to-play* (Integer/parseInt matches))
      (reset! *timeout* (Integer/parseInt timeout))
      (when web? (noir/start (inc (Integer/parseInt port)) {}))
      (server-loop on-connection-created :port (Integer/parseInt port)))))
