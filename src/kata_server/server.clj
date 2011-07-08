(ns kata-server.server
  (:import [java.net ServerSocket]))

(def *port* 8000)

(defn create-server-socket [] 
  (ServerSocket. *port*))

(defn server-loop [callback] 
  (with-open [serv-sock (create-server-socket)]
    (loop []
      (let [sock (.accept serv-sock)
            f (partial callback sock)
            thread (Thread. f)]
        (do
          (.start thread)
          (recur))))))
