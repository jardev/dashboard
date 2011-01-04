(ns net.jardev.dashboard.services.web
  (:use [net.jardev.dashboard.web.routes :only [app]]
        [ring.adapter.jetty])
  (:require [net.jardev.dashboard.config :as config]
            [clojure.contrib.logging :as logging]))

(defn- log [msg & vals]
  (let [line (apply format msg vals)]
    (logging/info line)))

;; ALGO
;; For every expired if
(defn do-sync []
  "Check available ETAs and send messages for expired ones"
  nil)

(defn start []
  (log "Starting notifications service...")
  (while true
    (do-sync)
    (Thread/sleep 60000)))
