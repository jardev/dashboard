(ns dashboard.db
  (:use [somnium.congomongo]
        [dashboard.utils])
  (:import java.util.Date
           [java.security MessageDigest]))


(def MAX-PAST-ETA-COUNT 10)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
  (let [user (or
              (fetch-one :users :where {:username username-or-email})
              (fetch-one :users :where {:email username-or-email}))]
    (when user
      (merge user {:roles (set (:roles user))}))))

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

(defn find-eta [id]
  (fetch-one :eta :where {:_id id}))

(defn update-eta! [eta what when comment]
  (let [eta (merge eta {:what what
                        :when when
                        :comment comment})]
    (log "NEW ETA: %s" eta)
    (update! :eta (id eta) eta)
    eta))

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
  (let [new-eta (merge eta {:done (java.util.Date.)})]
    (update! :eta (id eta) new-eta)
    new-eta))

(defn miss-eta
  ([eta] (miss-eta eta (java.util.Date.)))
  ([eta time]
     (let [new-eta (merge eta {:missed time})]
       (update! :eta (id eta) new-eta)
       new-eta)))

(defn eta-notified
  ([eta] (eta-notified eta (java.util.Date.)))
  ([eta time]
     (let [new-eta (merge eta {:notified time})]
       (update! :eta (id eta) new-eta)
       new-eta)))

(defn find-current-user-eta [user]
  (let [now (java.util.Date.)]
    (fetch-one :eta
               :where {:done {:$exists false}
                       :username (:username user)
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
