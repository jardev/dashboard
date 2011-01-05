(ns dashboard.routes
  (:use [compojure.core :only [defroutes GET POST ANY]]
        [ring.middleware file file-info stacktrace]
        [ring.adapter.jetty]
        [sandbar core stateful-session auth form-authentication]
        [dashboard.middleware]
        [dashboard.config :only [get-config]])
  (:require [dashboard.views.base :as base-views]
            [dashboard.db :as db]
            [dashboard.auth :as auth]
            [dashboard.forms.new-eta :as forms-new-eta]
            [dashboard.handlers :as handlers]))


(def dashboard-security-policy
  [#"/login.*"             [:any :nossl]
   #"/permission-denied.*" :any
   #".*\.(css|js|png|gif)" [:any :any-channel]
   #".*"                   [#{"admin" "user"} :nossl]])


(defroutes dashboard-routes
  auth/routes
  (GET "/" [] (handlers/dashboard))
  (GET "/permission-denied" [request] handlers/permission-denied)
  forms-new-eta/routes
  (ANY "*" [request] handlers/handler404))

(defn make-app [debug?]
  (-> dashboard-routes
      (with-security dashboard-security-policy auth/dashboard)
      wrap-stateful-session
      (wrap-file "public")
      wrap-file-info
      (wrap-if debug? wrap-request-logging)
      wrap-exception-logging
      (wrap-if (not debug?) wrap-failsafe)
      (wrap-if debug? wrap-stacktrace)))

