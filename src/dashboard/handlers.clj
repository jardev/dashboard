(ns dashboard.handlers
  (:use [compojure.core]
        [ring.util.response]
        [sandbar core stateful-session auth]
        [dashboard.forms.new-eta :only [new-eta-form]]
        [dashboard.utils :only [log]])
  (:import [java.text SimpleDateFormat ParseException])
  (:require [dashboard.db :as db]
            [dashboard.views.base :as base-views]
            [dashboard.views.dashboard :as dashboard-views]))

(defn dashboard []
  (dashboard-views/dashboard (db/get-current-eta)
                             (db/get-noeta-users)))

(defn permission-denied [request]
  (log "Permission denied for user %s. Request=%s"
       (current-user)
       request)
  (base-views/permission-denied))

(defn handler404 [request]
  (base-views/page404 (:uri request)))