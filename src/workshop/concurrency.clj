(ns workshop.concurrency
  "Session 1c — Values across threads (~8 min).

  The payoff of immutability: sharing is free, and the one
  mutable thing (a reference) has atomicity built in.
  Evaluate forms one by one inside the (comment ...) blocks.")

;; -----------------------------------------------------------------
;; 1. Four threads, one value, zero locks
;; -----------------------------------------------------------------

(def numbers (vec (range 1000000)))

(comment

  ;; Four threads, one value. A value can't change, so nothing to guard.
  (mapv deref
        [(future (reduce + numbers))
         (future (apply max numbers))
         (future (count (filter even? numbers)))
         (future (reduce + (map #(* % %) numbers)))])
  )

;; -----------------------------------------------------------------
;; 2. A "write" is just a new value
;; -----------------------------------------------------------------

(def v [1 2 3])

(comment

  ;; Same vector, two threads, both "mutating" it. Each gets its own
  ;; new value. No locks, no copies, no conflict.
  [@(future (concat v [4 5 6]))
   @(future (rest v))]

  v                          ; still exactly what it was
  )

;; -----------------------------------------------------------------
;; 3. pmap — parallelism as a library function
;; -----------------------------------------------------------------
;; When data can't be mutated, "run this in parallel" needs no
;; coordination at all — so it's a function, not a framework.

(defn slow-square [x]
  (Thread/sleep 100)
  (* x x))

(comment

  (time (doall (map  slow-square (range 8))))   ; one thread
  (time (doall (pmap slow-square (range 8))))   ; change one letter
  )

;; -----------------------------------------------------------------
;; 4. Coordination: atom + swap!
;; -----------------------------------------------------------------
;; An atom is a mutable *reference* to an immutable value.
;; swap! applies a pure function (old -> new) atomically.

(def counter (atom 0))

(comment

  ;; Four threads hammer the same reference:
  (dotimes [_ 4]
    (future (dotimes [_ 1000000] (swap! counter inc))))

  ;; Eval repeatedly — climbs, every read consistent, no lost updates.
  ;; Same total every run, and no AtomicLong anywhere.
  @counter

  ;; Threads emitting into shared state — same one-liner shape:
  (def results (atom []))
  (dotimes [n 4]
    (future (swap! results conj (* n n))))

  @results                   ; all four arrive; order varies per run
  )

;; -----------------------------------------------------------------
;; 5. Coordinating SEVERAL references: refs + dosync (STM)
;; -----------------------------------------------------------------
;; An atom guards ONE reference. For invariants that span more than
;; one, Clojure ships software transactional memory: transactions
;; that compose changes to several refs, retried on conflict.

(def alice (ref 100))
(def bob   (ref 0))

(comment

  ;; 100 concurrent transfers, each moving 1 kr from alice to bob:
  (dotimes [_ 100]
    (future (dosync (alter alice - 1)
                    (alter bob   + 1))))

  ;; Read BOTH in one transaction — a consistent pair at ANY moment.
  ;; Sums to 100 mid-flight, every time. No lock ordering — no locks.
  (dosync [@alice @bob])
  )
