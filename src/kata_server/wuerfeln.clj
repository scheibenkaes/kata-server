(ns kata-server.wuerfeln)

(defn init-match [players] 
  (let [roster (apply hash-map (flatten (for [player players] [player 0])))]
    {:roster roster}))
