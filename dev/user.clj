(ns user
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.set :as set]
   [clojure.string :as string]
   [com.grzm.component.pedestal :as pedestal-component]
   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl :refer [set-init]]
   [modular.postgres]
   [background-processing.background-processor :as background-processor]
   [background-processing.enqueuer :as enqueuer]
   [playground.server]
   [playground.service]))

(defn dev-system
  []
  (component/system-map
   :service-map playground.server/dev-map
   :background-processor (background-processor/new :queue-name "cljtest")
   :enqueuer (enqueuer/new :queue-name "cljtest")
   :db (modular.postgres/map->Postgres {:url "jdbc:postgresql:ebdb" :user "root" :password ""})
   :pedestal (component/using (pedestal-component/pedestal (constantly playground.server/dev-map))
                              playground.service/components-to-inject)))

(set-init (fn [_]
            (dev-system)))
