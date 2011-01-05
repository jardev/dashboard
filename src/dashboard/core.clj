(ns dashboard.core
  (:use [dashboard.routes :only [make-app]]
        [ring.adapter.jetty]
        [dashboard.utils :only [log]]
        [dashboard.config :only [get-config]]
        [somnium.congomongo :only [mongo!]])
  (:require [dashboard.notify :as notify]))


(defn run []
  ;; Initialize database
  (log "Setting database")
  (mongo! :db (get-config :mongo :db)
          :host (get-config :mongo :host)
          :port (get-config :mongo :port))

  ;; Run jetty
  (log "Running Embedded Jetty")
  (let [app (make-app (get-config :debug))]
    (run-jetty app
               {:port (get-config :web :port)
                :host (get-config :web :host)
                :join? false}))
  ;; Run notifier
  (notify/start)

  (log "All services are running")
  0)





