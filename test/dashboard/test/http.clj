(ns dashboard.test.http
  (:use [dashboard.routes] :reload)
  (:use [clojure.test]
        [dashboard.config]
        [dashboard.utils]
        [ring-test core])
  (:require [somnium.congomongo :as mongo]
            [net.cgrand.enlive-html :as html]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fixtures and init functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn init-tests [f]
  ;; Load configuartion
  (load-config! "test/config")
  (mongo/mongo! :db (get-config :mongo :db)
                :host (get-config :mongo :host)
                :port (get-config :mongo :port))
  ;; Clear the database
  (mongo/drop-database! (get-config :mongo :db))
  (mongo/set-database! (get-config :mongo :db))

  ;; Initial fixtures
  (mongo/insert! :users
                 {:_id (uuid)
                  :username "user"
                  :email "user@localhost.localdomain"
                  :roles ["user" "user"]
                  :password (sha1 "password")})
  ;; Build routes
  (rebuild-app)
  (use-app! dashboard-app)

  ;; Run test
  (f))

(defn login []
  (request :post "/login" {:username "user"
                           :password "password"}))

(defn init-http [f]
  (clear-cookies)
  (f))

(use-fixtures :each init-tests init-http)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest test-dashboard-auth
  (with-response (request :get "/")
    (check200)
    (is (= "/login" (get-current-uri)))
    (body-contains #"Welcome to Dashboard!")
    (body-contains #"Username:")
    (body-contains #"Password:")
    (body-contains-tags [[:input (html/attr= :name "username")]])
    (body-contains-tags [[:input (html/attr= :name "password")]])
    (body-contains-tags [[:input (html/attr= :value "Login")]])
    (body-contains-tags [[:input (html/attr= :value "Reset")]])
    (submit-form 0 {:username "user"
                    :password "password"}))
  (with-response (request :get "/")
    (check200)
    (body-contains #"Dashboard")
    (body-contains #"Current Status:")
    (body-contains [:a] #"Home")
    (body-contains [:a] #"ETA")
    (body-contains [:a] #"Logout")))

(deftest test-new-eta
  (login)
  (with-response (request :get "/eta")
    (body-contains #"ETA")
    (body-contains #"Add New ETA")
    (body-contains #"What's next?")
    (body-contains #"When?")
    (body-contains #"Comment")
    (with-response (submit-form 0 {:what "online"
                                   :when "20:00"
                                   :comment "Comment"})
      (is (= "/" (get-current-uri)))
      (body-contains #"online")
      (body-contains #"20:00")
      (body-contains #"Comment")
      (is (= 1 (count (mongo/fetch :eta :where {:what "online"})))))))

(deftest test-edit-eta
  (login)
  (with-response (request :post "/eta" {:what "online"
                                        :when "20:00"})
    (let [eta (mongo/fetch-one :eta)]
      (with-response (request :get (format "/eta/%s" (:_id eta)))
        (body-contains #"ETA")
        (body-contains #"Edit ETA")
        (body-contains #"What's next?")
        (body-contains #"When?")
        (body-contains #"Comment")
        (with-response (submit-form 0 {:what "offline"})
          (is (= "/" (get-current-uri)))
          (body-not-contains #"online")
          (body-contains #"offline")
          (is (= 0 (count (mongo/fetch :eta :where {:what "online"}))))
          (let [eta (mongo/fetch-one :eta)]
            (is (= "offline" (:what eta)))
            ;; Patch eta with created-date lower than now - :dashboard :eta-edit-timeout
            (mongo/update! :eta {:_id (:_id eta)}
                           (merge eta {:created (java.util.Date.
                                                 (.getYear (:created eta))
                                                 (.getMonth (:created eta))
                                                 (.getDate (:created eta))
                                                 0
                                                 0)}))
            (is (= 404 (:status (request :get (format "/eta/%s" (:_id eta))))))))))))






