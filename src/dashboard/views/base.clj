(ns dashboard.views.base
  (:use [sandbar core auth]
        [hiccup.core]
        [hiccup.page-helpers]))

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

(defn page404 [uri]
  (layout
   "Page Not Found"
   [:div
    [:h2 "404 Page Not Found"]
    [:p
     "You requested " (escape-html uri)]
    [:p
     "This page is not found"]
    [:p
     "Return to " (link-to "/" "Home")]]))

(defn permission-denied []
  (layout "Permission Denied"
          [:div
           [:h2 "Oops"]
           "You do not have enough permissions"]))
