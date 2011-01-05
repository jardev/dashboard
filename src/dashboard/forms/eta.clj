(ns dashboard.forms.eta
  (:use [sandbar.validation :only [build-validator
                                   non-empty-string
                                   add-validation-error]]
        [sandbar.auth :only [current-username]]
        [sandbar.core :only [cpath]]
        [compojure.core :only [defroutes]]
        [dashboard.utils]
        [dashboard.config :only [get-config]])
  (:import [java.text SimpleDateFormat ParseException]
           [java.util Date])
  (:require [dashboard.db :as db]
            [dashboard.views.base :as base-views]
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

(defn save-eta [data]
  (let [user (db/find-user (current-username))
        what (:what data)
        eta (parse-when (:when data))
        comment (:comment data)
        id (:_id data)]
    ;; Close all ETAs as done
    (if-not id
      ;; Not id - create a new ETA
      (do
        (doseq [eta (db/find-not-done-user-eta user)]
          (db/done-eta eta))
        ;; Create a new ETA
        (db/new-eta user what eta comment))
      ;; Existing ETA - check if we can save it
      (let [e (db/find-eta id)]
        (if (can-edit-eta? e {})
          (db/update-eta! e what eta comment)
          (throw-404))))))

(defn load-eta [id]
  (let [eta (db/find-eta id)]
    (if (can-edit-eta? eta {})
      (merge eta
             {:when (format-date (:when eta))})
      (throw-404))))

(def labels
  {:what "What's next?"
   :when "When?"
   :comment "Comment"
   :url "URL"
   :tags "Tags"})

(def register-eta-validator
  (build-validator (non-empty-string :what :when labels)
                   :ensure
                   when-validator))

(defmethod forms/template :data-form [_
                                      action
                                      {:keys [buttons title attrs]}
                                      field-table]
  (let [buttons (or buttons [[:submit] [:reset]])]
    [:div {:class "sandbar-form"}
     (forms/form-to [:post (cpath action) attrs]
              [:div {:class "form-header"}
               [:table
                [:tr
                 [:td {:colspan 2}
                  [:span {:class "form-title"} title]]]]]
              (forms/append-buttons-to-table field-table buttons))]))

(forms/defform eta-form
  "/eta"
  :fields [(forms/hidden :_id)
           (forms/textfield :what)
           (forms/textfield :when)
           (forms/textfield :url)
           (forms/textfield :tags)
           (forms/textarea :comment {:rows 5 :cols 70})]
  :properties labels
  :style :data-form
  :validator register-eta-validator
  :on-cancel "/"
  :load #(load-eta %)
  :on-success #(do (save-eta %) "/")
  :title #(case % :add "Add New ETA" "Edit ETA")
  :buttons [[:save] [:cancel]]
  :defaults #(load-default-values %))


(defroutes routes
  (eta-form (fn [request form] (base-views/form-layout "ETA" form))))

