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

(defn login []
  (do-post "/login"
           {:username "jardev"
            :password "qwertyuiop"}))

(deftest test-dashboard-auth
  (clear-cookies)
  (with-go200 "/"
    (body-contains #"Welcome to Dashboard!")
    (body-contains #"Username:")
    (body-contains #"Password:")
    (body-contains-tags [[:input (html/attr= :name "username")]])
    (body-contains-tags [[:input (html/attr= :name "password")]])
    (body-contains-tags [[:input (html/attr= :value "Login")]])
    (body-contains-tags [[:input (html/attr= :value "Reset")]])
    (submit-form 0
                 {:username "jardev"
                  :password "qwertyuiop"}))
  (with-go200 "/"
    (body-contains #"Dashboard")
    (body-contains #"Current Status:")
    (body-contains [:a] #"Home")
    (body-contains [:a] #"ETA")
    (body-contains [:a] #"Logout")))

(deftest forms-test
  (login)
  (with-go200 "/forms"
    (body-contains #"Forms")
    (println (pr-str
    (submit-form 0
                 {:input1 "check1"
                  :input2 "radio2"
                  :whoa ["value1" "value2" ""]
                  :large-text "Some Large Text"
                  :small-text "small text"
                  :password "pswd"}
                 :button1)))
    ))
