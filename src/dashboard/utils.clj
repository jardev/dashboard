(ns dashboard.utils
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
