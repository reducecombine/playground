(ns playground.jobs.sample
  (:require
   [background-processing.background-job :as background-job]
   [io.pedestal.log :as log]))

(defrecord Sample [temperature]
  background-job/BackgroundJob
  (perform [_]
    (log/error ::temperature (* 100 temperature)))
  (type [_]
    ::background-job/cpu-bound))

(defn new [temperature]
  (map->Sample {:temperature temperature}))
