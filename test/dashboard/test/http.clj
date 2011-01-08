(ns dashboard.test.http
  (:use [dashboard.routes] :reload)
  (:use [clojure.test]
        [dashboard.config]
        [ring-test core])
  (:require [somnium.congomongo :as congo]
            [net.cgrand.enlive-html :as html]))

(load-config! "sites/default")
(congo/mongo! :db (get-config :mongo :db)
              :host (get-config :mongo :host)
              :port (get-config :mongo :port))
(rebuild-app)
(use-app! dashboard-app)

(deftest test-dashboard-auth
  (clear-cookies)
  (with-go200 "/"
    (body-contains #"Welcome to Dashboard!")
    (body-contains #"Username:")
    (body-contains #"Password:")
    (body-contains-tags [[:input (html/attr= :name "username")]])
    (body-contains-tags [[:input (html/attr= :name "password")]])
    (body-contains-tags [[:input (html/attr= :value "Login")]])
    (body-contains-tags [[:input (html/attr= :value "Reset")]]))
  (do-post "/login" {:username "jardev"
                     :password "qwertyuiop"})
  (with-go200 "/"
    (body-contains #"Dashboard")
    (body-contains #"Current Status:")
    (body-contains [:a] #"Home")
    (body-contains [:a] #"ETA")
    (body-contains [:a] #"Logout")))

