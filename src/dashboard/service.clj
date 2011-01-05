(ns dashboard.service
  (:use [dashboard.utils :only [log]]
        [dashboard.core :only [run]]
        [dashboard.config :only [get-config load-config!]]))


(defn init [& args]
  (let [site (first args)]
    (log "Loading settings for %s" site)
    (load-config! site))
  0)


(defn start []
  (log "Running service")
  (run))