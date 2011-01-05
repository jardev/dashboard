(ns dashboard.middleware
  (:use [dashboard.utils]))


(defn wrap-request-logging [handler]
  (fn [{:keys [request-method uri] :as req}]
    (let [start (System/currentTimeMillis)
          resp (handler req)
          finish (System/currentTimeMillis)
          total (- finish start)]
      (log "Request %s %s (%dms)" request-method uri total)
      resp)))

(defn wrap-if [handler pred wrapper & args]
  (if pred
    (apply wrapper handler args)
    handler))

(defn wrap-exception-logging [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (log-exception e)
        (throw e)))))

(defn wrap-failsafe [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        {:status 500
        :headers {"Content-Type" "text/plain"}
         :body "We're sorry, something went wrong."}))))
