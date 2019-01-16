(ns playground.services.invoices.update.logic)

(defn to-update [id new-email]
  {:update :users
   :set    {:email new-email}
   :where  [:= :id id]})
