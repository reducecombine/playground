(ns playground.services.invoices.delete.logic
  (:require
   [honeysql.helpers :as hh]))

(defn to-delete []
  {:delete-from :users
   :where       [:in :id {:select [:id]
                          :from   [:users]
                          :limit  1}]})

(defn to-serialize []
  {:select [:%count.*]
   :from   [:users]})
