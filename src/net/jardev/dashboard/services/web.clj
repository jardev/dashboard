(ns net.jardev.dashboard.services.web
  (:use [net.jardev.dashboard.web.routes :only [app]]
        [ring.adapter.jetty])
  (:require [net.jardev.dashboard.config :as config]
            [clojure.contrib.logging :as logging]))


(defn- log [msg & vals]
  (let [line (apply format msg vals)]
    (logging/info line)))

(defn start []
  (log "Starting web server...")
  (run-jetty (var app)
             {:port config/port
              :host config/host
              :join? false}))
