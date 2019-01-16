(ns playground.coerce
  (:require
   [playground.spec-utils :refer [check!]]
   [spec-coerce.core :as coerce]
   [clojure.spec.alpha :as spec]))

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

(defn coerce-map-indicating-invalidity [spec v]
  (let [v (coerce/coerce spec v)]
    (if (spec/valid? spec v)
      v
      (assoc v ::invalid? true))))

(comment
  ;; creating a FSM validates that it's indeed finite and deterministic.
  ;; also all names must be namespaced.
  (def fsm (fsm/new {:states      #{::stopped ::running ::flying}
                     :initial     ::stopped
                     :transitions {::stopped {::run! ::running}
                                   ::running {::fly!  ::flying
                                              ::stop! ::stopped}
                                   ::flying  {::land! ::running}}}))
  fsm ;; {... :current-state ::stopped :current-event nil}
  (fsm/advance fsm ::stop!) ;; error
  (fsm/advance fsm ::run!) ;; 1) returns a new fsm (immutability). 2) associates an event:
  ;; {... :current-state ::running :current-event ::run!}
  ;; ^ things can subscribe to ::run!, but that's not `fsm`'s responsibility
  ;; IMPORTANT: :current-event is private, so subscriptions are guaranteedly exhaustive. Easiest way: {:private-state {:current-event :foo}}
  (def app (atom {:widget-state fsm}))
  (defn attrs-for [app status transition]
    {:enabled  (-> app :widget-state (fsm/transitionable-to? ::stopped))
     :on-click (fn [_]
                 (framework/emit! app :widget-state fsm/advance ::stop!))})
  (defn widget [app]
    [:div
     [:div "Stop" (attrs-for app ::stopped ::stop!)]
     [:div "Run" (attrs-for app ::running ::run!)]
     [:div "Fly" (attrs-for app ::flying ::fly!)]])
  (framework/subscribe-to-data-changes! ::widget-transitions
                                        (fn [app]
                                          (let [fsm   (-> app :widget-state)
                                                event (fsm/derive-event fsm
                                                                        ;; here we make it impossible to forget handling some event
                                                                        {::stop! ::widget-transitions.stop!
                                                                         ::run!  ::widget-transitions.run!
                                                                         ::fly!  ::widget-transitions.fly!
                                                                         ::land! ::widget-transitions.land!})]
                                            (framework/emit! app event))))
  (framework/subscribe-to-event! ::widget-transitions.stop!
                                 (fn [app]
                                   ;; do anything reacting to the `stop!`: show a flash message, animation, etc
                                   ))

  ;; when a FSM is actually finite/deterministic and its handlers exhasustively listed in advance,
  ;; we can create generative tests:

  ;; -do we uncover plain bugs?
  ;; -do we encounter illegal states/transitions when interacting in certain ways?

  ;; Finally, the more FSMs reside in our codebase, the more we can simulate intrincate/repetitive user interactions.
  ;; e.g., can playing with invoices (FSM 1) break the login form (FSM 2)?
  ;; this testing becomes possible with fsms + side-effect-free arch, in a generative, instant fashion.

  )
