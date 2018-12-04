(ns playground.services.invoices.insert.endpoint
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.spec.alpha :as spec]
   [honeysql.core :as h]
   [playground.services.invoices.insert.logic :as logic]))

(spec/def ::amount nat-int?)

(spec/def ::api (spec/keys :req-un [::amount]))

(defn perform [{{:keys [amount]} :query-params :keys [db] :as request}]
  (let [db (->> db :pool (hash-map :datasource))
        insert (-> (logic/to-insert amount (java.util.UUID/randomUUID))
                   (h/format))
        fetch (h/format logic/to-query)
        _ (jdbc/execute! db insert)
        result (-> (jdbc/query db fetch)
                   (logic/to-serialize))]
    {:status 200
     :body result}))
