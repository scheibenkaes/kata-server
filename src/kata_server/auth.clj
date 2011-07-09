(ns kata-server.auth
  (:use [clojure.string :only [split]]
        [clojure.java.io :only [reader writer]])
  (:require [kata-server.socket :as socket])
  (:use kata-server.player))

(def helo #"HELO (.+)")

(defn authenticate [sock] 
  (when-let [line (socket/read-line sock)]
    (when-let [cmd (re-find helo (.trim line))]
      (let [n (second cmd)]
        (with-meta (new-player n) {:socket sock :reader (reader sock) :writer (writer sock)})))))
