(ns ring-test.core
  (:use [clojure.test]
        [net.cgrand.enlive-html :as html])
  (:import [java.io ByteArrayInputStream]))


(def *app* (fn []))
(def *response* nil)
(def *cookies* (atom {}))

(defn clear-cookies []
  (swap! *cookies* (fn [_] {})))

(defn get-cookies []
  @*cookies*)

(defn set-cookies! [cookie]
  ;; TODO: Log (println (str "SET-COOKIE!: " cookie))
  (when cookie
    (swap! *cookies*
           (fn [cookies]
             (merge cookies
                    (reduce (fn [accum el]
                              (let [items (.split el "=")
                                    key (.trim (first items))
                                    value (.trim (second items))]
                                (merge accum {key {:value value}})))
                            {}
                            (.split cookie ";")))))))

(defmacro with-app [app & code]
  `(binding [*app* ~app]
     ~@code))

(defn use-app! [app]
  (alter-var-root #'*app* (fn [_] app)))

(defmacro with-response [op & code]
  `(binding [*response* ~op]
     ~@code))

(defn request
  ([method uri params]
     (request method uri params {}))
  ([method uri params request-params]
     (when *app*
       (let [res (*app* (merge request-params
                               {:request-method method
                                :uri uri
                                :params params
                                :cookies (get-cookies)}))]
         ;; TODO: Log (println (str "RESPONSE: " res))
         (when (:headers res)
           (set-cookies! (first ((:headers res) "Set-Cookie"))))
         (if (== (:status res) 302)
           (request :get ((:headers res) "Location") {})
           res)))))

(defn do-get
  ([uri] (do-get uri {} {}))
  ([uri params] (do-get uri params {}))
  ([uri params request-params]
     (request :get uri params request-params)))

(defn do-post
  ([uri] (do-post uri {} {}))
  ([uri params] (do-post uri params {}))
  ([uri params request-params]
     (request :post uri params request-params)))

(defn check200 []
  (when *response*
    (is (= 200 (:status *response*)))))

(defmacro with-go200 [uri & code]
  `(with-response (do-get ~uri)
     (check200)
     ~@code))

(defn go200 [uri]
  (with-response (do-get uri)
    (check200)))

(defn get-body []
  (:body *response*))

(defn body-contains
  ([selector pattern]
     (let [text (map html/text
                     (html/select (html/html-resource (ByteArrayInputStream.
                                                       (.getBytes (:body *response*))))
                                  selector))
           contains? (reduce (fn [res t] (or res (re-find pattern t)))
                          false
                          (reverse text))]
       (is contains? (str "Search for \""
                       pattern
                       "\" in "
                       (pr-str text)))))
  ([pattern]
     (is (re-find pattern (:body *response*))
         (str "Search for \"" pattern "\" in " (:body *response*)))))

(defn body-not-contains
  ([selector pattern]
     (let [text (map html/text
                     (html/select (html/html-resource (ByteArrayInputStream.
                                                       (.getBytes (:body *response*))))
                                  selector))
           not-contains? (reduce (fn [res t] (and res (not (re-find pattern t))))
                          true
                          (reverse text))]
       (is not-contains? (str "Search for \""
                       pattern
                       "\" in "
                       (pr-str text)))))
  ([pattern]
     (let [not-contains? (not (re-find pattern (:body *response*)))]
     (is not-contains?
         (str "Search for \"" pattern "\" in " (:body *response*))))))

(defn text= [s]
  (html/pred #(= s (html/text %))))

(defn body-contains-tags [selector]
  (let [res (html/select (html/html-resource (ByteArrayInputStream.
                                              (.getBytes (:body *response*))))
                         selector)
        not-empty? (not (empty? res))]
    (is not-empty?
        (str "Search for tags by " selector))))

