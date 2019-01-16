(ns playground.data-protocols-poc)


(defn logic [user input]
  {:validation/valid?    (every? input #{:foo :bar}) ;; insight: `:validation/valid?` doesn't need to be a fn. Just a value. This greatly reduces complexity
   :crud/to-insert       (select-keys input [:foo :bar])
   :commands/name        ::Creation
   :logging/component-id ::component
   :auth/who             user})
