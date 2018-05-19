(ns playground.coerce
  (:require
   [playground.spec-utils :refer [check!]]
   [spec-coerce.core :as coerce]))

(defn coerce
  ([v]
   (coerce/coerce-structure v))
  ([spec v]
   (coerce/coerce spec v)))

(defn coerce!
  [spec v]
  (let [v (coerce/coerce spec v)]
    (check! spec v)
    v))

