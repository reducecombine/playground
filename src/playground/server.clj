(ns playground.server
  (:gen-class)
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as server]
   [io.pedestal.http.route :as route]
   [playground.service :as service]
   [com.grzm.component.pedestal :as pedestal-component]
   [background-processing.background-processor :as background-processor]
   [background-processing.enqueuer :as enqueuer]))

(def dev-map
  (-> service/service ;; start with production configuration
      (merge {:env                     :dev
              ;; do not block thread that starts web server
              ::server/join?           false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::server/routes          #(route/expand-routes (deref #'service/routes))
              ;; all origins are allowed in dev mode
              ::server/allowed-origins {:creds true :allowed-origins any?}
              ;; Content Security Policy (CSP) is mostly turned off in dev mode
              ::server/secure-headers  {:content-security-policy-settings {:object-src "none"}}})
      server/default-interceptors
      server/dev-interceptors))

(defn production-system []
  (let [queue-name "playground-production"
        production-map (-> service/service
                     (merge {:env :production})
                     (server/default-interceptors))]
    (component/system-map
     :service-map production-map
     :background-processor (background-processor/new :queue-name queue-name)
     :enqueuer (enqueuer/new :queue-name queue-name)
     :db (modular.postgres/map->Postgres {:url "jdbc:postgresql:ebdb" :user "root" :password ""})
     :pedestal (component/using (pedestal-component/pedestal (constantly production-map))
                                service/components-to-inject))))

(defn -main [& args]
  (-> (production-system) (component/start)))

(defn test?
  [service-map]
  (-> service-map :env #{:test}))

(defrecord Pedestal [service-map service]
  component/Lifecycle
  (start [this]
    (if service
      this
      (cond-> service-map
        true                      server/create-server
        (not (test? service-map)) server/start
        true                      ((partial assoc this :service)))))

  (stop [this]
    (when (and service (not (test? service-map)))
      (server/stop service))
    (dissoc this :service)))
