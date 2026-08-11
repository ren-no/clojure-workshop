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

  ;; Four threads, all consuming the SAME value. No locks,
  ;; no copies — a value can't change, so there's nothing to guard.
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
  ;; Here we pass the same vector to two threads, both "mutates" the vector.
  ;; Each thread gets a new value consistent with its own view of the world. No locks, no copies, no conflicts.
  [@(future (concat v [4 5 6]))   ;; one thread's new value => (1 2 3 4 5 6)
   @(future (rest v))]            ;; the other's new value  => (2 3)

  ;; v is still exactly what it was, and the two threads never conflicted.
  v
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

  (time (doall (map  slow-square (range 8))))   ;; ~800 ms — one thread
  (time (doall (pmap slow-square (range 8))))   ;; ~100 ms — change one letter
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

  @counter   ;; eval repeatedly — watch it climb, every read consistent
  ;; => 4000000 when done. Every run. No lost updates, no AtomicLong.

  ;; Threads emitting values into shared state — same one-liner shape:
  (def results (atom []))
  (dotimes [n 4]
    (future (swap! results conj (* n n))))

  @results   ;; all four arrive: [0 1 4 9] — order varies run to run
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

  ;; Read BOTH in one transaction — a consistent pair at ANY moment:
  (dosync [@alice @bob])
  ;; sums to 100 mid-flight, every time; => [0 100] when done.
  ;; No deadlocks, no lock ordering — no locks.
  )
