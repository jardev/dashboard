(ns net.jardev.dashboard.web.routes
  (:use [compojure.core :only [defroutes GET POST ANY]]
        [ring.middleware.file]
        [ring.middleware.file-info]
        [sandbar core stateful-session auth form-authentication validation]
        [ring.adapter.jetty]
        [net.jardev.dashboard.web.forms.new-eta :only [new-eta-routes]])
  (:require [net.jardev.dashboard.web.views :as views]
            [net.jardev.dashboard.api.db :as db]
            [net.jardev.dashboard.web.handlers :as handlers]))


(defauth dashboard-auth
  :type :form
  :load (fn [username password]
          (merge (db/find-user username)
                 {:login-password password}))
  :validator #(if (db/check-password % (:login-password %))
                %
                (add-validation-error % "Incorrect username or password!"))
  :properties {:username "Username:"
               :password "Password:"
               :username-validation-error "Please enter a username!"
               :password-validation-error "Please enter a password!"})

(def dashboard-security-policy
  [#"/login.*"             [:any :nossl]
   #"/permission-denied.*" :any
   #".*\.(css|js|png|gif)" [:any :any-channel]
   #".*"                   [#{"admin" "user"} :nossl]])

(defroutes dashboard-routes
  (dashboard-auth (fn [request content] (views/form-layout "Welcome to Dashboard!"
                                                           [:div
                                                            [:h3 "Please authorize"]
                                                            content])))
  (GET "/" [] (handlers/dashboard))
  (GET "/permission-denied" [] (handlers/permission-denied))
  new-eta-routes
  (ANY "*" [] (views/home)))

(def app
  (-> dashboard-routes
      (with-security dashboard-security-policy dashboard-auth)
      wrap-stateful-session
      (wrap-file "public")
      wrap-file-info))

(defn run []
  (run-jetty (var app) {:join? false
                        :port 8000}))




