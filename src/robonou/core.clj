(ns robonou.core
  (:require [discord.bot :as bot]))

(defn -main
  "Starts a Discord bot."
  [& args]
  (bot/start))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
