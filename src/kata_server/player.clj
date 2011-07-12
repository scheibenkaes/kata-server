(ns kata-server.player)

(def allowed-chars #"[a-zA-Z]{3,8}")

(defn legal-name? [s] 
  (if s 
    (not (nil? (re-matches allowed-chars s)))
    false))

(defrecord Player [name])

(defn new-player [n] 
  (Player. (keyword n)))
