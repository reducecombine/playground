(ns playground.background-processing
  (:require
   [amazonica.aws.sqs :as sqs]
   [better-cond.core :refer [cond] :rename {cond with}]
   [clojure.core.async :as async :refer [go]]
   [com.stuartsierra.component :as component]
   [io.pedestal.log :as log]))

(defrecord BackgroundProcessor [queue-name running?]
  component/Lifecycle
  (start [this]
    (reset! running? true)
    (let [queue (sqs/find-queue queue-name)
          runner (future
                   (while @running?
                     (with
                      :when-let [message (-> (sqs/receive-message :queue-url queue :wait-time-seconds 1)
                                             :messages
                                             first)]
                      :let [runnable (-> message :body read-string)]
                      (try
                        (runnable)
                        (-> message (assoc :queue-url queue) sqs/delete-message)
                        (catch Throwable e
                          (log/error ::start.exception e :start.message runnable))))))]
      (assoc this :future runner)))

  (stop [this]
    (some-> this :future future-cancel)
    (reset! running? false)))

(defn new [& {:keys [queue-name]}]
  (map->BackgroundProcessor {:queue-name queue-name
                             :running? (atom false)}))

