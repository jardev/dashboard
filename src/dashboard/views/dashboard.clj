(ns dashboard.views.dashboard
  (:use [hiccup.core]
        [hiccup.page-helpers]
        [sandbar core stateful-session auth]
        [dashboard.utils :only [format-date]]
        [dashboard.views.base :only [layout]])
  (:import [java.util Date]
           [java.text SimpleDateFormat]))


(defn show-eta [eta not-done-class]
  [:tr {:class (str (:row-class eta)
                    (if (:done eta)
                      " eta-done"
                      (str " " not-done-class)))}
   [:td.who (:username eta)]
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
   "Dashboard "
   [:div
    [:div#users
     [:ul
     (for [user noeta-users]
       [:li.red (:username user)])]]
    [:h3 "Current Status:"]
    [:table.eta
     (for [eta (add-cycles (:future etas))]
       (show-eta eta nil))
     (show-now (:now etas))
     (for [eta (add-cycles (:past etas))]
       (show-eta eta "missed"))]]
   [:meta {:http-equiv "refresh"
           :content "30; /"}]))
