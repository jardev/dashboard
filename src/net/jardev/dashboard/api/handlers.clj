(ns net.jardev.dashboard.api.handlers
  (:use clojure.contrib.json.write
        clojure.contrib.json.read
        clojure.contrib.duck-stream
        compojure.core
        net.jardev.dashboard.api.db)
  (:import net.jardev.dashboard.api)

(defn- emit-json
  "Turn the object to JSON and emit it with the correct content type"
  [x]
  {:headers {"Content-Type" "application/json"}
   :body (json-str {:content x})})

(defn eta-path
  "Returns the relative URL for ETA"
  [eta]
  (str "/eta/" (:_id eta)))

(defn with-url
  "Associates task with :url pointing to its relative URL"
  [eta]
  (assoc eta :url (eta-path eta)))


