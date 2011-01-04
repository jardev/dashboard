(ns net.jardev.dashboard.web.views
  (:use [hiccup.core]
        [hiccup.page-helpers]
        [sandbar core stateful-session auth])
  (:import [java.util Date])
  (:import [java.text SimpleDateFormat]))

(defn layout [title content & extra-head]
  (html
   (doctype :html4)
   [:html
    [:head
     [:title title]
     (stylesheet "styles.css")
     (stylesheet "sandbar-forms.css")
     extra-head
     (icon "dashboard.png")]
    [:body
     [:div#page
      [:div#header [:h1 title]]
      (when (current-user)
        [:div#commands
         [:ul
          [:li (current-username)]
          [:li (link-to "/" "Home")]
          [:li (link-to "/new-eta/" "ETA")]
          [:li (link-to "/logout" "Logout")]]])
      [:div#content
       content]]]]))

(defn form-layout [title content & extra-head]
  (layout
   title
   [:table
    [:tr
     [:td {:style "width:50%"} ""]
     [:td {:style "width:450px"}
      content]
     [:td {:style "width:50%"} ""]]]))

(defn login []
  (layout
   [:div
    [:form {:method "post"}
     "User name: " [:input {:name "username" :type "text"}] [:br]
     "Password: " [:input {:name "password" :type "password"}] [:br]
     [:input {:type "submit" :value "Login"}]]]))

(defn format-date [date]
  (let [now (Date.)]
    (.format (SimpleDateFormat.
              (if (and (== (.getYear now) (.getYear date))
                       (== (.getMonth now) (.getMonth date))
                       (== (.getDate now) (.getDate date)))
                "HH:mm"
                "yyyy-MM-dd HH:mm"))
              date)))

(defn show-eta [eta]
  [:tr {:class (str (:row-class eta) (when (:done eta) " eta-done"))}
   [:td.who (:username (:user eta))]
   [:td.when (format-date (:when eta))]
   [:td.what
    [:b (escape-html (:what eta))]
    (when (:comment eta)
      (list "&nbsp;&nbsp;" [:i [:small (escape-html (:comment eta))]]))
    [:br]
    [:small.smallnote
     (format-date (:created eta))
     (when (:done eta)
       (str ", done(" (format-date (:done eta)) ")"))]]])

(defn show-now [now]
  [:tr.eta-now
   [:td]
   [:td.when [:b (format-date now)]]
   [:td]])

(defn add-cycles [coll]
  (map #(assoc %1 :row-class %2) coll (cycle ["row1" "row2"])))

(defn dashboard [etas noeta-users]
  (layout
   "Dashboard"
   [:div
    [:div#users
     [:ul
     (for [user noeta-users]
       [:li.red (:username user)])]]
    [:h3 "Current Status:"]
    [:table.eta
     (for [eta (add-cycles (:future etas))]
       (show-eta eta))
     (show-now (:now etas))
     (for [eta (add-cycles (:past etas))]
       (show-eta eta))]]
   [:meta {:http-equiv "refresh"
           :content "30; /"}]))

(defn home []
  (layout "Welcome!"
   [:h2 "Welcome!"]))






