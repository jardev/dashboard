(ns dashboard.core
  (:use [dashboard.routes :only [rebuild-app dashboard-app]]
        [ring.adapter.jetty]
        [dashboard.utils :only [log]]
        [dashboard.config :only [get-config]]
        [somnium.congomongo :only [mongo!]])
  (:require [dashboard.notify :as notify]
            [swank.swank :as swank]))


(defn run []
  ;; Initialize database
  (log "Setting database")
  (mongo! :db (get-config :mongo :db)
          :host (get-config :mongo :host)
          :port (get-config :mongo :port))

  ;; Run jetty
  (log "Running Embedded Jetty")
  (rebuild-app) ;; Trick for using slime
  (run-jetty #'dashboard-app
             {:port (get-config :web :port)
              :host (get-config :web :host)
              :join? false})
  ;; Run notifier
  (notify/start)

  (when (get-config :debug)
    ;; Start Swank
    (log "Starting Swank")
    (swank/start-repl))

  (log "All services are running")
  0)
