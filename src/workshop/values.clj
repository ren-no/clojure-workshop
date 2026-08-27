(ns workshop.values
  "Session 1a — The value of values (follows slide 11, ~10 min).

  Evaluate forms one by one inside the (comment ...) blocks.
  Nothing here is typed live: rehearse by walking top to bottom.")

;; -----------------------------------------------------------------
;; 1. Identity vs equality
;; -----------------------------------------------------------------
;; JS:    {a: 1} === {a: 1}                      => false
;; Java:  new int[]{1,2}.equals(new int[]{1,2})  => false
;; Both compare places. Clojure compares VALUES.

(comment

  (= {:a 1} {:a 1})
  (= [1 2 3] [1 2 3])
  (= '(1 2 3) [1 2 3])            ; across collection types, too

  ;; Because maps are values, they can be KEYS in other maps...
  (def routes
    {{:method :get  :path "/tickets"} 'list-tickets
     {:method :post :path "/tickets"} 'create-ticket})

  (get routes {:method :get :path "/tickets"})

  ;; ...and live in sets. Try this with mutable objects and hashCode.
  (def seen #{{:host "web-1" :port 443}
              {:host "web-2" :port 443}})

  (contains? seen {:host "web-1" :port 443})
  )

;; -----------------------------------------------------------------
;; 2. "Updates" return new values — the old one is untouched
;; -----------------------------------------------------------------

(comment

  (def config {:retries 3
               :timeout 5000
               :endpoints {:api "https://api.internal"}})

  (def batch-config (assoc config :timeout 60000))

  batch-config
  config                          ; unchanged. Always.

  ;; Nested updates, still non-destructive:
  (assoc-in config [:endpoints :metrics] "https://metrics.internal")
  (update config :retries inc)

  ;; The slide-2 bug is now impossible to write: a log of snapshots
  ;; can never change after the fact.
  (def audit-log
    [config
     (assoc config :timeout 60000)
     (assoc config :retries 5)])

  (map :timeout audit-log)        ; history is free
  )

;; -----------------------------------------------------------------
;; 3. "Isn't copying everything slow?" — structural sharing
;; -----------------------------------------------------------------
;; Persistent vectors are wide trees (32-way tries). assoc copies one
;; PATH (~log32 n nodes) and shares the rest.

(comment

  (def big (vec (range 1000000)))

  (time (mapv identity big))            ; a full copy, for contrast
  (time (assoc big 500000 :changed))    ; ~4 small nodes, rest shared

  (def big2 (assoc big 500000 :changed))
  (nth big  500000)
  (nth big2 500000)
  )
