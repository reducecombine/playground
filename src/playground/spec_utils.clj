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

(defn describe-map-spec [spec]
  (->> spec spec/get-spec spec/describe* rest (partition 2) (mapv vec) (into {})))

(defmacro instrumenting [f spec & body]
  `(let [~f (let [{:keys [~'args ~'ret]} ~spec]
              (fn [& fnargs#]
                {:pre  [(check! ~'args fnargs#)]
                 :post [(check! ~'ret ~'%)]}
                (apply ~f fnargs#)))]
     ~@body))

(comment
  ;; basic usage
  (let [the-spec (spec/fspec :args (spec/cat :x number?)
                             :ret number?)
        the-fn   (fn [x]
                   x)]
    (instrumenting the-fn the-spec
                   (the-fn "1")))
  ;; original intended usage: Data Protocols
  ;; protocol ns:
  (spec/def ::do-it (spec/fspec :args (spec/cat :x number?)
                                :ret number?))
  ;; impl ns:
  (defn logic [input user]
    {::protocol/do-it (fn [x]
                        x)
     ::command/name   ::Creation})
  ;; other ns:
  (let [logic (logic/logic input user)
        spec  ::protocol/do-it
        doer  (spec logic)]
    (instrumenting doer spec
                   (doer 53)))
  )
