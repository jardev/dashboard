(ns dashboard.config)

(def *config* {})

(defn load-config! [file]
  (alter-var-root #'*config*
                  (fn [_]
                    (load-file file))))

(defn get-config [& args]
  (reduce #(if %1 (%2 %1) %1) *config* args))
