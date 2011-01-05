(ns net.jardev.dashboard.services.notify
  (:import [java.util Date]
           [java.text SimpleDateFormat])
  (:require [net.jardev.dashboard.config :as config]
            [net.jardev.dashboard.api.db :as db]
            [com.draines.postal.core :as postal]
            [clojure.contrib.logging :as logging]))

(defn- log [msg & vals]
  (let [line (apply format msg vals)]
    (logging/info line)))

(defn- timedelta-gt [t1 t2 delta]
  (> (/ (- (.getTime t1) (.getTime t2)) 60000) delta))

(defn format-date [date]
  (let [now (Date.)]
    (.format (SimpleDateFormat.
              (if (and (== (.getYear now) (.getYear date))
                       (== (.getMonth now) (.getMonth date))
                       (== (.getDate now) (.getDate date)))
                "HH:mm"
                "yyyy-MM-dd HH:mm"))
              date)))

(defn- build-mail-body [eta]
  (let [user (db/find-user (:username eta))]
    (format config/notify-body
            (or (:first-name user)
                (:last-name user)
                (:username user))
            (:what eta)
            (format-date (:when eta))
            config/site)))

(defn notify-missed-eta
  "Send e-mail for ETAs user"
  ([eta] (notify-missed-eta eta (java.util.Date.)))
  ([eta date]
     ;; Send a message
     (let [user (db/find-user (:username eta))
           email (:email user)]
       (log "Sending notification for @%s" (:username eta))
       (when email
         (when (= 0 (:code (postal/send-message {:from config/notify-from
                                                 :to email
                                                 :subject config/notify-subject
                                                 :body (build-mail-body eta)})))
           ;; Set notified-flag
           (db/eta-notified eta date))))))


(defn do-sync []
  "Check available ETAs and send messages for expired ones"
  (let [now (java.util.Date.)]
    (doseq [eta (db/find-not-done-eta now)]
      ;; Set missed flag if not set
      (let [missed (or (:missed eta)
                       (:missed (db/miss-eta eta now)))]
        ;; Notify
        (if (:notified eta)
          (when (timedelta-gt now (:notified eta) config/notify-timeout)
            (notify-missed-eta eta))
          (when (timedelta-gt now missed config/eta-timeout)
            (log "Missed %s" missed)
            (notify-missed-eta eta)))))))

(defn start []
  (log "Starting notifications service...")
  (while true
    (do-sync)
    (Thread/sleep 30000)))
