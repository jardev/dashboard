(ns dashboard.routes
  (:use [compojure.core :only [defroutes GET POST ANY]]
        [ring.middleware file file-info stacktrace]
        [ring.adapter.jetty]
        [sandbar core stateful-session auth form-authentication]
        [dashboard.middleware]
        [dashboard.utils]
        [dashboard.config :only [get-config]])
  (:require [hozumi.mongodb-session :as mongoss]
            [dashboard.views.base :as base-views]
            [dashboard.db :as db]
            [dashboard.auth :as auth]
            [dashboard.forms.eta :as forms-eta]
            [dashboard.handlers :as handlers]))


(def dashboard-security-policy
  [#"/login.*"             [:any :nossl]
   #"/form.*" :any
   #"/permission-denied.*" :any
   #".*\.(css|js|png|gif)" [:any :any-channel]
   #".*"                   [#{:admin :user} :nossl]])


(defroutes dashboard-routes
  auth/routes
  (GET "/" [] (handlers/dashboard))
  (GET "/permission-denied" [request] handlers/permission-denied)
  forms-eta/routes
  (ANY "*" [request] handlers/handler404))

(defn make-app [debug?]
  (-> dashboard-routes
      (wrap-if debug? wrap-request-logging)
      wrap-exception-logging
      (wrap-if (not debug?) wrap-failsafe)
      (wrap-if debug? wrap-stacktrace)
      wrap-exception-404
      (with-security dashboard-security-policy auth/dashboard)
      wrap-user-roles
      (wrap-stateful-session {:store (mongoss/mongodb-store)})
      (wrap-file "public")
      wrap-file-info
      wrap-charset))

(def dashboard-app (make-app (get-config :debug)))

(defn rebuild-app []
  "Used as a trick for slime"
  (def dashboard-app (make-app (get-config :debug))))