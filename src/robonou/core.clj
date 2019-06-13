(ns robonou.core
  (:require [discljord.connections :as c]
            [discljord.messaging :as m]
            [discljord.events :as e]
            [clojure.core.async :as a]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]))


;; todo publish to netlify
(defn citation-du-jour []
  (let [body (:body (client/get "http://evene.lefigaro.fr/citations"))
        node (html/html-snippet body)]
    (-> (html/select node [:.figsco__quote__text])
        first :content first)))

(def settings (clojure.edn/read-string (slurp "data/settings.edn")))
(def links (clojure.edn/read-string (slurp "data/links.edn")))
(def token (:token settings))
(def state (atom {::mode ::game-math}))
(def channel {::bot-test 580121292121047062})



;; ------------
(def command-list
  {:command-list/help "Affiche l'aide"
   :command-list/commands "Affiche les commandes disponibles"
   :command-list/links "Affiche les liens utiles"
   :command-list/hidden-commands "A tes risques et périls"
   :command-list/day-quote "La citation du jour"})

(def command-list-hidden
  {:command-list-hidden/lewp ""
   :command-list-hidden/will ""
   :command-list-hidden/gif "Fais péter un gif"
   :command-list-hidden/robonou ""
   :command-list-hidden/clj-eval "Clojure repl eval"})

(defmulti command
  (fn [command-name]
    command-name))

(defmethod command :command-list/help
  [command-name]
  "Bienvenue sur l'aide intéractive, tape !commands pour afficher la liste des commandes")

(defmethod command :command-list-hidden/lewp
  [command-name]
  "Dommage, Hugo joue à LoL, essayes plus tard")

(defmethod command :command-list-hidden/will
  [command-name]
  "Dommage, William dors, essayes plus tard")

(defmethod command :command-list-hidden/gif
  [command-name]
  "TODO afficher un gif marrant")

(defmethod command :command-list-hidden/robonou
  [command-name]
  "Robonou à ton service")

(defmethod command :command-list-hidden/clj-eval
  [command-name message]
  (eval (read-string message)))

(defmethod command :command-list/commands
  [command-name]
  (->> (for [[key message] command-list]
         (let [key (str "!" (name key))]
           (str key " - " message)))
       (clojure.string/join "\n")))

(defmethod command :command-list/hidden-commands
  [command-name]
  (->> (for [[key message] command-list-hidden]
         (let [key (str "!" (name key))]
           (str key " - " message)))
       (clojure.string/join "\n")))

(defmethod command :command-list/day-quote
  [command-name]
  (citation-du-jour))

(defmethod command :command-list/links
  [command-name]
  (apply str (map (fn [link]
                    (str (:links/name link) "\n"
                         (when (:links/url link)
                           (str (:links/url link) "\n")) 
                         (:links/description link) "\n"
                         "\n")
                    )
                  links)
         ))

(defn command-str [command-name]
  (str "!" (name command-name)))


(defmulti handle-event
  (fn [event-type event-data]
    event-type))

(defmethod handle-event :default
  [event-type event-data])

(defmethod handle-event :connexion
  [event-type event-data])

(defmethod handle-event :message-create
  [event-type {{bot :bot} :author :keys [channel-id content]}]
  (when-not bot
    (doall
     (for [[command-key] (merge command-list command-list-hidden)]
       (when (= content (command-str command-key))
         (m/create-message! (:messaging @state) channel-id :content (command command-key)))))))

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
