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
  (GET "/forms" []
       (base-views/layout "Forms"
                          [:div
                           [:form {:action "/form1" :method :post}
                            [:input {:type :checkbox :name :input1 :value "check1"}]
                            [:input {:type :checkbox :name :input1 :value "check2"}]
                            [:input {:type :radio :name :input2 :value "radio1"}]
                            [:input {:type :radio :name :input2 :value "radio2"}]
                            [:select {:name "whoa" :multiple true}
                             [:option {:id "id1" :name "name1" :value "value1"} "Option1"]
                             [:option {:id "id2" :name "name2" :value "value2"} "Option2"]
                             [:option {:id "id3" :name "name3" :value ""} "Option3"]]
                            [:textarea {:name "large-text"}]
                            [:input {:name "small-text" :type "text"}]
                            [:input {:name "password" :type "password"}]
                            [:input {:type :submit :name "button1" :value "Button1"}]
                            [:input {:type :submit :name "button2" :value "Button2"}]]
                           [:form {:action "" :method :post}
                            [:input {:type :text :name :input1}]
                            [:input {:type :submit}]]
                           [:form {:action "/form3" :method :post}
                            [:input {:type :text :name :input1}]
                            [:input {:type :submit}]]]))
  (POST "/form1" [request] (fn [request]
                             {:status 200
                              :headers {"Content-Type" "text/plain"}
                              :body (pr-str (:params request))}))

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