(ns dashboard.auth
  (:use [compojure.core :only [defroutes]]
        [sandbar core stateful-session auth form-authentication validation])
  (:require [dashboard.db :as db]
            [dashboard.views.base :as base-views]))


(defauth dashboard
  :type :form
  :load (fn [username password]
          (let [user (db/find-user username)]
            (when user
              (merge user
                     {:login-password password
                      :roles (set (:roles user))}))))
  :validator #(if (db/check-password % (:login-password %))
                %
                (add-validation-error % "Incorrect username or password!"))
  :properties {:username "Username:"
               :password "Password:"
               :username-validation-error "Please enter a username!"
               :password-validation-error "Please enter a password!"})

(defroutes routes (dashboard
                   (fn [request content]
                     (base-views/form-layout
                      "Welcome to Dashboard!"
                      [:div
                       [:h3 "Please authorize"]
                       content]))))