(ns robonou.core
  (:require [discljord.connections :as c]
            [discljord.messaging :as m]
            [discljord.events :as e]
            [clojure.core.async :as a]))

(def settings (clojure.edn/read-string (slurp "data/settings.edn")))
(def token (:token settings))
(def state (atom nil))
(def channel {::bot-test 580121292121047062})

(defmulti command
  (fn [command-name]
    command-name))

(defmethod command :command-list/help
  [command-name]
  {:key (name command-name)
   :message "Bienvenue sur l'aide intéractive, tapez !comands pour afficher la liste des commandes"})

(defmethod command :command-list/commands
  [command-name]
  (->> (for [[key message] command-list]
         (let [key (str "!" (name key))]
           (str key " - " message)))
       (clojure.string/join "\n")))

(def command-list
  {:command-list/help "Affiche l'aide"
   :command-list/commands "Affiche les commandes disponibles"})

(defn command-str [command-name]
  (str "!" (name command-name)))

(comment
  (m/create-message! (:messaging @state)
                     (::bot-test channel)
                     :content "Salut")

  @(m/get-channel! (:messaging @state) (::bot-test channel)))


(defmulti handle-event
  (fn [event-type event-data]
    event-type))

(defmethod handle-event :default
  [event-type event-data])

(defmethod handle-event :message-create
  [event-type {{bot :bot} :author :keys [channel-id content]}]
  (cond 
    (= content "!disconnect")
    (a/put! (:connection @state) [:disconnect])

    (= content (command-str :command-list/commands))
    (when-not bot
      (m/create-message! (:messaging @state) channel-id :content (command :command-list/commands)))

    (= content (command-str :command-list/help))
    (when-not bot
      (m/create-message! (:messaging @state) channel-id :content (command :command-list/help))) 
    
    ))

(defn -main
  [& args]
  (let [event-ch (a/chan 100)
        connection-ch (c/connect-bot! token event-ch)
        messaging-ch (m/start-connection! token)
        init-state {:connection connection-ch
                    :event event-ch
                    :messaging messaging-ch}]
    (reset! state init-state)
    (e/message-pump! event-ch handle-event)
    (m/stop-connection! messaging-ch)
    (c/disconnect-bot! connection-ch)))


(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
