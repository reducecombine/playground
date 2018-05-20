(ns playground.jobs.sample
  (:require [io.pedestal.log :as log]))

(defrecord Sample [temperature]
  clojure.lang.IFn
  (invoke [_]
    (log/error ::temperature (* 100 temperature))))

(defn new [temperature]
  (map->Sample {:temperature temperature}))
