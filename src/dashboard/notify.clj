(ns dashboard.notify
  (:use [dashboard.utils]
        [dashboard.config :only [get-config]])
  (:import [java.util Date]
           [java.text SimpleDateFormat])
  (:require [dashboard.db :as db]
            [com.draines.postal.core :as postal]))


(defn build-mail-body [eta]
  (let [user (db/find-user (:username eta))]
    (format (get-config :notify :email-body)
            (or (:first-name user)
                (:last-name user)
                (:username user))
            (:what eta)
            (format-date (:when eta))
            (get-config :web :site))))

(defn notify-missed-eta
  "Send e-mail for ETAs user"
  ([eta] (notify-missed-eta eta (java.util.Date.)))
  ([eta date]
     ;; Send a message
     (let [user (db/find-user (:username eta))
           email (:email user)]
       (log "Sending notification for @%s" (:username eta))
       (when email
         (when (= 0 (:code (postal/send-message {:from (get-config :notify :email-from)
                                                 :to email
                                                 :subject (get-config :notify :email-subject)
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
          (when (timedelta-gt now (:notified eta) (get-config :notify :notify-timeout))
            (notify-missed-eta eta))
          (when (timedelta-gt now missed (get-config :notify :eta-timeout))
            (log "Missed %s" missed)
            (notify-missed-eta eta)))))))

(defn start []
  (daemon #(while true
             (do-sync)
             (Thread/sleep 30000))))
