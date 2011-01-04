(ns net.jardev.dashboard.api.db
  (:use somnium.congomongo)
  (:import java.util.Date
           [java.security MessageDigest]))

(mongo! :db "dashboard")

(def MAX-PAST-ETA-COUNT 10)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- sha1 [obj]
  (let [bytes (.getBytes (with-out-str (pr obj)))
        res (new StringBuilder)
        digest (apply vector (.digest (MessageDigest/getInstance "SHA-1") bytes))]
    (dotimes [i (count digest)]
      (.append res (Integer/toString (bit-and (digest i) 0xff) 16)))
    (str "sha1$" res)))

(defn- uuid []
  (str (java.util.UUID/randomUUID)))

(defn with-uuid
  "Return obj with new generated UUID in:_id"
  [obj]
  (assoc obj :_id (uuid)))

(defn id [obj]
  {:_id (:_id obj)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Users
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn find-all-users []
  (fetch :users))

(defn find-user [username-or-email]
  (or
   (fetch-one :users :where {:username username-or-email})
   (fetch-one :users :where {:email username-or-email})))

(defn add-user [user]
  (when (find-user (:username user))
    (throw (new Exception "User already exists")))
  (insert! :users
           {:_id (uuid)
            :username (:username user)
            :email (:email user)
            :first-name (:first-name user)
            :last-name (:last-name user)
            :roles (:roles user)
            :password (sha1 (:password user))}))

(defn change-password [user password]
  (update! :users (id user) (merge user {:password (sha1 password)})))

(defn delete-user [user]
  (destroy! :users (id user)))

(defn check-password [user password]
  (= (:password user) (sha1 password)))

(defn authenticate-user [user password]
  (merge user {:authenticated (check-password user password)}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ETA
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn new-eta [user what when comment]
  (insert! :eta
           (with-uuid
             {:created (java.util.Date.)
              :username (:username user)
              :what what
              :when when
              :comment comment})))

(defn find-all-eta  []
  (fetch :eta))

(defn get-current-eta
  "Return current expectations"
  ([] (get-current-eta MAX-PAST-ETA-COUNT))
  ([count]
     (let [now (java.util.Date.)]
       {:future (reverse
                 (sort-by :when
                          (fetch :eta :where {:when {:$gte now}})))
        :now now
        :past (take count
                    (reverse
                     (sort-by :when
                              (fetch :eta
                                     :where {:when {:$lte now}}))))})))

(defn done-eta [eta]
  (update! :eta (id eta) (merge eta {:done (java.util.Date.)})))

(defn miss-eta
  ([eta] (miss-eta eta (java.util.Date.)))
  ([eta time]
     (update! :eta (id eta) (merge eta {:missed time}))))

(defn eta-notified
  ([eta] (eta-notified eta (java.util.Date.)))
  ([eta time]
     (update! :eta (id eta) (merge eta {:notified time}))))

(defn find-current-user-eta [user]
  (let [now (java.util.Date.)]
    (fetch-one :eta
               :where {:done {:$exists false}
                       :user (:username user)
                       :when {:$gte now}})))

(defn find-not-done-user-eta [user]
  (fetch :eta
         :where {:done {:$exists false}
                 :username (:username user)}))

(defn find-not-done-eta
  ([] (find-not-done-eta (java.util.Date.)))
  ([date-time]
     (fetch :eta
            :where {:done {:$exists false}
                    :when {:$lt date-time}})))


(defn get-noeta-users
  "Return users which do not have ETA"
  []
  (let [now (java.util.Date.)]
    (filter #(not (find-current-user-eta %)) (find-all-users))))
