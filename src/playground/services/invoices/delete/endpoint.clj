(ns playground.services.invoices.delete.endpoint
  (:require
   [clojure.java.jdbc :as jdbc]
   [honeysql.core :as h]
   [playground.services.invoices.delete.logic :as logic]))

(defn perform [{:keys [db] :as request}]
  (let [db (->> db :pool (hash-map :datasource))
        _  (->> (logic/to-delete)
                (h/format)
                (jdbc/execute! db))
        response (->> (logic/to-serialize)
                      (h/format)
                      (jdbc/query db)
                      (first))]
    {:status 200
     :body   response}))
