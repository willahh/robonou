(ns robonou.game.math)

(def state (atom {::status-list [:status/start :status/stop]
                  ::status :status/stop
                  ::game-steps [:step/start
                                :step/define-number-of-players]
                  ::game-step 0
                  ::number-of-players 0}))

(defn start []
  (swap! state assoc ::status ::start))

(defn next-step []
  (::game-step @state))

(defn echo []
(case (::status @state)
  :status/stop "Stopped"
  :status/start "The game has started"))
