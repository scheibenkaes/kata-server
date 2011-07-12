(ns kata-server.auth
  (:require [kata-server.socket :as socket])
  (:use kata-server.player))

(def helo #"HELO (.+)")

(defn authenticate [sock] 
  (when-let [line (socket/receive-line sock)]
    (when-let [cmd (re-find helo (.trim line))]
      (let [n (second cmd)]
        (with-meta (new-player n) {:socket sock})))))
