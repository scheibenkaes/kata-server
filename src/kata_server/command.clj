(ns kata-server.command)

(defmacro defcommand [cmd-name re & values]
  (let [mapping (apply hash-map values)]
    `(def ~cmd-name 
       (fn [line#] 
         (let [search# (re-find ~re line#)
               found# (for [[k# v#] ~mapping] [k# (search# v#)])]
           (into {} found#))))))

(defcommand helo #"HELO (.*) .*" :name 1) ; => (helo "HELO TestUser") => {:name "TestUser"}
