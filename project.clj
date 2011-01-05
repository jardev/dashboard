(defproject dashboard "0.0.4"
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
                 [com.draines/postal "1.4.0-SNAPSHOT"]
                 [lein-daemon "0.2.1"]]
  :dev-dependencies [[lein-daemon "0.2.1"]
                     [swank-clojure "1.2.1"]]
  :jvm-opts ["-Xms8m" "-Xmx64m"])

(load-file "load-sites.clj")