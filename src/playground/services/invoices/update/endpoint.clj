(ns playground.services.invoices.update.endpoint
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.spec.alpha :as spec]
   [playground.services.invoices.update.logic :as logic]
   [honeysql.core :as h]))

(spec/def ::id nat-int?)

(spec/def ::email string?)

(spec/def ::api (spec/keys :req-un [::id ::email]))

(defn perform [{{:keys [id email]} :params
                :keys              [db]
                :as                request}]
  (let [db (->> db :pool (hash-map :datasource))]
    (try
      (jdbc/execute! db (h/format (logic/to-update id email)))
      (catch Exception e
        (throw (-> e .getNextException))))
    {:status 200
     :body   "OK"}))
