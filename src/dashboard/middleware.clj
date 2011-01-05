(ns dashboard.middleware
  (:use [dashboard.utils]
        [clojure.contrib.condition]
        [dashboard.handlers :only [handler404]]))



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
        (log "Request: %s" req)
        (throw e)))))

(defn wrap-failsafe [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        {:status 500
        :headers {"Content-Type" "text/plain"}
         :body "We're sorry, something went wrong."}))))

;; This middleware must be before exception handler but after sandbars middleware
(defn wrap-exception-404 [handler]
  (fn [req]
    (handler-case :type
      (handler req)
      (handle :e404
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body (handler404 req)}))))


(defn wrap-charset
  ([handler] (wrap-charset handler "utf8"))
  ([handler charset]
     (fn [request]
       (if-let [response (handler request)]
         (if-let [content-type (get-in response [:headers "Content-Type"])]
           (if (.contains content-type "charset")
             response
             (assoc-in response
                       [:headers "Content-Type"]
                       (str content-type "; charset=" charset)))
           response)))))
