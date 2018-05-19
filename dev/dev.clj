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
   [clojure.tools.namespace.repl :refer [refresh refresh-all clear]]
   [com.grzm.component.pedestal :as pedestal-component]
   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl :refer [reset set-init start stop system]]
   [modular.postgres]
   [playground.server]
   [playground.service]))

;; NOTE: Do not try to load source code from 'resources' directory
(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")

(defn dev-system
  []
  (component/system-map
   :service-map playground.server/dev-map
   :db (modular.postgres/map->Postgres {:url "localhost" :user "postgres" :password "postgres"})
   :pedestal (component/using (pedestal-component/pedestal (constantly playground.server/dev-map))
                              playground.service/components-to-inject)))

(set-init (fn [_]
            (dev-system)))
