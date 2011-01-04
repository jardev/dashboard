(ns net.jardev.dashboard.web.forms.new-eta
  (:use [sandbar.validation :only [build-validator
                                   non-empty-string
                                   add-validation-error]]
        [sandbar.auth :only [current-username]]
        [compojure.core :only [defroutes]])
  (:import [java.text SimpleDateFormat ParseException])
  (:require [net.jardev.dashboard.api.db :as db]
            [net.jardev.dashboard.web.views :as views]
            [sandbar.forms :as forms]))


(defn parse-when [value]
  (let [s (.trim value)
        now (java.util.Date.)
        res (try
              (.parse (SimpleDateFormat. "yyyy-MM-dd HH:mm") s)
              (catch ParseException e
                (try
                  (let [d (.parse (SimpleDateFormat. "HH:mm") s)]
                    (.setYear d (.getYear now))
                    (.setMonth d (.getMonth now))
                    (.setDate d (.getDate now))
                    d))))]
    res))

(defn when-validator [data]
  (try
    (let [value (parse-when (:when data))
          now (java.util.Date.)]
      (if (.before value now)
        (add-validation-error data :when "Please use future ETA")
        data))
    (catch ParseException e
      (add-validation-error data :when (.getMessage e)))))


(defn- load-default-values [request]
  (let [current (db/find-current-user-eta (db/find-user (current-username)))]
    (merge {}
           (when current
             {:what (:what current)
              :comment (:comment current)}))))

(defn save-new-eta [data]
  (let [user (db/find-user (current-username))
        what (:what data)
        eta (parse-when (:when data))
        comment (:comment data)]
    ; Close all ETAs as done
    (doseq [eta (db/find-not-done-user-eta user)]
      (db/done-eta eta))
    ; Create a new ETA
    (db/new-eta user what eta comment)))

(def labels
  {:what "What's next?"
   :when "When?"
   :comment "Comment"})

(def register-new-eta-validator
  (build-validator (non-empty-string :what :when labels)
                   :ensure
                   when-validator))

(forms/defform new-eta-form
  "/new-eta/"
  :fields [(forms/textfield :what)
           (forms/textfield :when)
           (forms/textarea :comment {:rows 5 :cols 70})]
  :properties labels
  :validator register-new-eta-validator
  :on-cancel "/"
  :on-success #(do (save-new-eta %) "/")
  :title (fn [_] "Add new ETA")
  :buttons [[:add] [:cancel]]
  :defaults #(load-default-values %))


(defroutes new-eta-routes
  (new-eta-form (fn [request form] (views/form-layout "Add new ETA" form))))

