(ns robonou.game.math)

(def state (atom {::status-list [:status/start :status/stop]
                  ::status :status/stop
                  ::game-steps {:step/game-step-1 :step/stop
                                :step/game-step-2 :step/define-number-of-players
                                :step/game-step-3 :step/define-name-of-players}
                  ::game-step :step/game-step-1
                  ::number-of-players 0}))

(defn start []
  (swap! state assoc ::status ::start))

(defn next-step []
  (case (::game-step @state)
    :step/game-step-1
    (swap! state assoc ::game-step :step/game-step-2)))

(defn echo []
  (case (::game-step @state)
    :step/game-step-1
    "Démarrer le jeu ? !oui !non"
    
    :step/game-step-2
    "Le jeu démarre, combien de joueurs ?"
    
    :step/game-step-3
    "Etape 2"))


(defn input-handler [text]
  (if (= text "!oui")
    (do (next-step)
        (echo))))


(case (::status @state)
  :status/stop "Stopped"
  :status/start "The game has started")