(ns dashboard.utils
  (:use [clojure.contrib.condition :only [raise]]
        [sandbar core auth]
        [dashboard.config :only [get-config]])
  (:import [java.security MessageDigest]
           [java.util Date]
           [java.text SimpleDateFormat])
  (:require [clojure.contrib.logging :as logging]
            [clj-stacktrace.repl :as strp]))

(defn sha1 [obj]
  (let [bytes (.getBytes (with-out-str (pr obj)))
        res (new StringBuilder)
        digest (apply vector (.digest (MessageDigest/getInstance "SHA-1") bytes))]
    (dotimes [i (count digest)]
      (.append res (Integer/toString (bit-and (digest i) 0xff) 16)))
    (str "sha1$" res)))

(defn uuid []
  (str (java.util.UUID/randomUUID)))

(defn log [msg & vals]
  (let [line (apply format msg vals)]
    (logging/info line)))

(defn format-date [date]
  (let [now (Date.)]
    (.format (SimpleDateFormat.
              (if (and (== (.getYear now) (.getYear date))
                       (== (.getMonth now) (.getMonth date))
                       (== (.getDate now) (.getDate date)))
                "HH:mm"
                "yyyy-MM-dd HH:mm"))
              date)))

(defn timedelta-gt [t1 t2 delta]
  (> (/ (- (.getTime t1) (.getTime t2)) 60000) delta))

(defn timedelta-lt [t1 t2 delta]
  (<= (/ (- (.getTime t1) (.getTime t2)) 60000) delta))

(defn log-exception [e]
  (log "Exception:\n%s" (strp/pst-str e)))

(defn daemon
  "Creates a new daemon thread and sets runnable to f"
  [f]
  (let [tf (fn []
             (try
               (f)
               (catch Exception e
                 (log-exception e))))
        t (Thread. tf)]
    (do
      (.setDaemon t true)
      (.start t)
      t)))

(defn throw-404 []
  (raise :type :e404 :arg 'value :value 404))

(defn can-edit-eta? [eta {:keys [now username delta]}]
  (let [now (or now (Date.))
        delta (or delta (get-config :dashboard :eta-edit-timeout) 2)
        username (or username (current-username))]
    (or (any-role-granted? "admin")
        (and eta
             (not (:done eta))
             (not (:missed eta))
             (= (:username eta) username)
             (timedelta-lt now (:created eta) delta)))))