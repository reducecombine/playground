(ns playground.spec-utils
  (:require
   [clojure.spec.alpha :as spec]
   [expound.alpha :as expound]))

(defn check! [& args]
  (doseq [[spec x] (partition 2 args)]
    (or (spec/valid? spec x)
        (do
          (expound/expound spec x)
          (throw (ex-info "Validation failed" {:explanation (spec/explain-str spec x)})))))
  true)
