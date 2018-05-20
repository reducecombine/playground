(ns playground.background-processing
  (:require
   [clojure.core.async :as async :refer [go]]
   [com.stuartsierra.component :as component]
   [io.pedestal.log :as log]))

(defrecord BackgroundProcessor [queue running]
  component/Lifecycle
  (start [this]
    (reset! running true)
    (let [runner (future
                   (while @running
                     (let [[val port] (async/alts!! [queue (async/timeout 100)])]
                       (when (= port queue)
                         (log/error :temperature val)))))]
      (assoc this :future runner)))

  (stop [this]
    (some-> this :future future-cancel)
    (reset! running false)))

(defn new []
  (map->BackgroundProcessor {:queue (async/chan)
                             :running (atom false)}))
