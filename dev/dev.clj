(ns dev
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application.

  Call `(reset)` to reload modified code and (re)start the system.

  The system under development is `system`, referred from
  `com.stuartsierra.component.repl/system`.

  See also https://github.com/stuartsierra/component.repl"
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
   :db (modular.postgres/map->Postgres {:url "jdbc:postgresql:ebdb" :user "vemv" :password ""})
   :pedestal (component/using (pedestal-component/pedestal (constantly playground.server/dev-map))
                              playground.service/components-to-inject)))

(set-init (fn [_]
            (dev-system)))
