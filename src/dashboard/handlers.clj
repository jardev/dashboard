(ns dashboard.handlers
  (:use [compojure.core]
        [ring.util.response]
        [sandbar core stateful-session auth]
        [dashboard.forms.eta :only [eta-form]]
        [dashboard.utils]
        [dashboard.config :only [get-config]])
  (:import [java.util Date])
  (:require [dashboard.db :as db]
            [dashboard.views.base :as base-views]
            [dashboard.views.dashboard :as dashboard-views]))

(defn dashboard []
  (let [etas (db/get-current-eta)
        eta-now (:now etas)
        eta-future (for [eta (:future etas)]
                     (assoc eta :can-edit (can-edit-eta? eta
                                                         {:now eta-now})))
        eta-past (:past etas)]
    (dashboard-views/dashboard eta-future eta-now eta-past
                               (db/get-noeta-users))))

(defn permission-denied [request]
  (log "Permission denied for user %s. Request=%s"
       (current-user)
       request)
  (base-views/permission-denied))

(defn handler404 [request]
  (base-views/page404 (:uri request)))
