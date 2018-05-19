(defproject playground "0.0.1-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [com.grzm/component.pedestal "0.1.7"]
                 [com.stuartsierra/component "0.3.2"]
                 [io.pedestal/pedestal.service "0.5.3"]
                 [io.pedestal/pedestal.jetty "0.5.3"]
                 [juxt.modular/postgres "0.0.1-SNAPSHOT"]
                 [ch.qos.logback/logback-classic "1.1.8" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.22"]
                 [org.slf4j/jcl-over-slf4j "1.7.22"]
                 [org.slf4j/log4j-over-slf4j "1.7.22"]
                 [prismatic/schema "1.1.9"]]
  :repl-options {:port 41234}
  :min-lein-version "2.0.0"
  :resource-paths ["config" "resources"]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "playground.server/run-dev"]}
                   :plugins [[cider/cider-nrepl "0.18.0-SNAPSHOT"]
                             [refactor-nrepl "2.4.0-SNAPSHOT"]]
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.3"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [com.stuartsierra/component.repl "0.2.0"]]
                   :source-paths ["dev"]}
             :uberjar {:aot [playground.server]}}
  :main ^{:skip-aot true} playground.server)

