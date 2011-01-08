(ns ring-test.core
  (:use [clojure.test]
        [net.cgrand.enlive-html :as html])
  (:import [java.io ByteArrayInputStream]))


(def *app* (fn []))
(def *response* nil)
(def *cookies* (atom {}))
(def *uri* nil)

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
     (binding [*uri* ~uri]
       ~@code)))

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

(defn get-body-resource []
  (html/html-resource (ByteArrayInputStream.
                       (.getBytes (:body *response*)))))

(defn body-not-contains
  ([selector pattern]
     (let [text (map html/text
                     (html/select (get-body-resource)
                                  selector))
           not-contains? (reduce (fn [res t]
                                   (and res
                                        (not (re-find pattern t))))
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

(defn ci-attr= [attr value]
  (html/pred #(let [av (-> % :attrs attr)]
                (when av
                  (= (.toLowerCase value)
                     (.toLowerCase av))))))

(defn- select-tags
  ([form tag name]
     (html/select form [[tag
                         (ci-attr= :name (.getName name))]]))
  ([form tag type name]
     (html/select form [[tag
                         (ci-attr= :type type)
                         (ci-attr= :name (.getName name))]])))

(defn find-tags [form key]
  (let [eon #(when % (if (empty? %) nil %))
        tags (or (eon (select-tags form :input "text" key))
                 (eon (select-tags form :input "password" key))
                 (eon (select-tags form :textarea key))
                 (eon (select-tags form :input "submit" key))
                 (eon (select-tags form :input "radio" key))
                 (eon (select-tags form :input "checkbox" key))
                 (eon (select-tags form :select key)))]
    (is tags (format "Searching tag for %s in %s" key form))
    ;(println (format "FORM=%s\r\nKEY=%s\r\nTAGS=%s" form key (pr-str tags)))
    tags))

(defmulti transform-tag-value (fn [tags value]
                                ;(println (format "TAGS=%s VALUE=%s" (pr-str tags) value))
                                (let [tag-description (first tags)
                                      tag (:tag tag-description)
                                      type (-> tag-description :attrs :type)]
                                  [tag (when type (.toLowerCase type))])))

(defmethod transform-tag-value [:input "text"] [tags value]
  value)

(defmethod transform-tag-value [:input "password"] [tags value]
  value)

(defmethod transform-tag-value [:textarea nil] [tags value]
  value)

(defmethod transform-tag-value [:input "submit"] [tags value]
  (-> tags first :attrs :value))

(defmethod transform-tag-value [:input "radio"] [tags value]
  (let [radio (html/select tags [(html/attr= :value value)])]
    (when-not (empty? radio)
      value)))

(defmethod transform-tag-value [:input "checkbox"] [tags value]
  (if (vector? value)
    (vec (for [v value]
           (when-not (empty? (html/select tags [(html/attr= :value v)]))
             v)))
    (when-not (empty? (html/select tags [(html/attr= :value value)]))
      value)))

(defmethod transform-tag-value [:select nil] [tags value]
  (let [check #(not (empty? (html/select tags [(html/attr= :value %)])))]
    (if (vector? value)
      (vec (for [v value]
             (when (check v)
               v)))
      (when (check value)
        value))))

(defn- transform-param [form key value]
  (let [form-value (transform-tag-value (find-tags form key) value)]
    (is form-value (pr-str "Check value for {%s %s} in %s"
                           key
                           value
                           form))
    (when (vector? form-value)
      (for [fv form-value]
        (is fv (pr-str "Check value for {%s %s} in %s"
                           key
                           value
                           form-value))))
    {key form-value}))

(defn- transform-values [form params]
  (reduce (fn [res param]
            (merge res
                   (transform-param form
                                    (first param)
                                    (second param))))
          {}
          params))

(defn submit-form
  ([index params] (submit-form index params nil))
  ([index params submit]
     (let [forms (html/select (get-body-resource) [:form])
           form (nth forms index)
           action (-> form :attrs :action)
           uri (if (empty? action) *uri* action)]
       (is (not (empty? uri)) uri)
       ;; Do post request checking that all fields in params
       ;; are belong to the form and have existing values
       (do-post uri (merge (transform-values form params)
                           (when submit
                             {submit ""}))))))
