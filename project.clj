(defproject dashboard "0.0.1"
  :description "Dashboard"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.3"]
                 [hiccup "0.3.1"]
                 [sandbar "0.3.3"]
                 [ring/ring-core "0.3.5"]
                 [ring/ring-devel "0.3.5"]
                 [ring/ring-jetty-adapter "0.3.5"]
                 [ring/ring-httpcore-adapter "0.3.5"]
                 [congomongo "0.1.3-SNAPSHOT"]
                 [org.clojars.sethtrain/postal "0.2.0"]
                 [lein-daemon "0.2.1"]]
  :dev-dependencies [[lein-daemon "0.2.1"]]
  :daemon {"web" {:ns "net.jardev.dashboard.services.web"
                  :options {:errfile "web.log"
                            :pidfile "web.pid"}}
            "notify" {:ns "net.jardev.dashboard.services.notify"
                      :options {:errfile "notify.log"
                                :pidfile "notify.pid"}}})
