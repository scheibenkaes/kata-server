(ns kata-server.player)

(defrecord Player [name])

(defn new-player [n] 
  (Player. (keyword n)))
