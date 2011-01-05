;; Load sites
(in-ns 'leiningen.core)

(defn- normalize-path [project-root path]
  (when path
    (let [f (File. path)]
      (.getAbsolutePath (if (.isAbsolute f) f (File. project-root path))))))

(defn join-path [& pathes]
  (reduce (fn [res path] (.toString (File. res path))) pathes))

;; Add additional daemons per each site in sites/
(defn load-sites []
  (let [root# (.getParent (File. *file*))
        normalize-path# (partial normalize-path root#)
        sites-path (normalize-path# (or (:sites-path project) "sites"))
        pid-path (normalize-path# (or (:pid-path project) "pid"))
        log-path (normalize-path# (or (:log-path project) "log"))
        user (:user project)
        daemons (reduce (fn [d file]
                          (println (format "Loading site '%s/%s'" sites-path file))
                          (assoc d file {:ns "net.jardev.dashboard.service"
                                         :options {:errfile (join-path log-path (format "%s.log" file))
                                                   :pidfile (join-path pid-path (format "%s.pid" file))
                                                   :user user}}))
                        {}
                        (.list (File. sites-path)))]
    (alter-var-root #'project
                    (fn [p] (merge p {:daemon daemons})))))

(load-sites)