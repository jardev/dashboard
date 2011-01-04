(ns net.jardev.dashboard.config)

;; ----------------------------------------------------------------------------
;; WEB
;; ----------------------------------------------------------------------------
(def site "http://dashboard.jardev.net")
(def port 8000)
(def host "127.0.0.1")

;; ----------------------------------------------------------------------------
;; Notification Service
;; ----------------------------------------------------------------------------
(def eta-timeout 2) ; 2 minutes
(def notify-timeout 10) ; 10 minutes
(def notify-from "dashboard@jardev.net")
(def notify-subject "You have missed your expectation!")
(def notify-body (str "Hi %s,\r\n\r\nYour expectation for \"%s\" (%s) is expired.\r\n"
               "Please set a new expectation at %s\r\n\r\n\r\n"
               "--\r\nThanks,\r\nDashboard"))


