(ns net.jardev.dashboard.web.handlers
  (:use compojure.core)
  (:use ring.util.response)
  (:use [net.jardev.dashboard.web.forms.new-eta :only [new-eta-form]])
  (:import [java.text SimpleDateFormat ParseException])
  (:require [net.jardev.dashboard.api.db :as db]
            [net.jardev.dashboard.web.views :as views]))

(defn dashboard []
  (views/dashboard (db/get-current-eta)
                   (db/get-noeta-users)))

(defn permission-denied []
  (views/layout "Permission Denied"
                [:div
                 [:h2 "Oops"]
                 "You do not have enough permissions"]))

