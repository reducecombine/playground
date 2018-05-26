(ns playground.enqueueing
  (:require
   [amazonica.aws.sqs :as sqs]
   [better-cond.core :refer [cond] :rename {cond with}]
   [clojure.core.async :as async :refer [go >!]]
   [com.stuartsierra.component :as component]
   [io.pedestal.log :as log]))

(defrecord Enqueuer [queue-name channel running?]
  component/Lifecycle
  (start [this]
    (reset! running? true)
    (let [queue (sqs/find-queue queue-name)
          runner (future
                   (while @running?
                     (with
                      :let [[message port] (async/alts!! [channel (async/timeout 100)])]
                      :when (= port channel)
                      :let [serialized (pr-str message)]
                      (try
                        (sqs/send-message queue serialized)
                        (catch Throwable e
                          (log/error ::start.exception e ::start.serialized serialized)
                          (>! channel message))))))]
      (assoc this :future runner)))

  (stop [this]
    (some-> this :future future-cancel)
    (reset! running? false)))

(defn new [& {:keys [queue-name channel]
              :or {channel (async/chan)}}]
  (map->Enqueuer {:queue-name queue-name
                  :channel channel
                  :running? (atom false)}))
