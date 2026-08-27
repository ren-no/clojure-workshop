(ns workshop.macros
  "Session 2 — Code is data (follows slide 12, ~15 min).

  Arc: code IS a data structure
    -> so ordinary functions can transform code
    -> so language features are libraries."
  (:require [clojure.repl :refer [source doc]]
            [clojure.walk :refer [macroexpand-all]]
            [clojure.pprint :as pprint]))

;; -----------------------------------------------------------------
;; 1. Code is data. Literally.
;; -----------------------------------------------------------------

(comment

  (+ 1 2)
  ;; => 3

  '(+ 1 2)
  ;; => (+ 1 2)      a LIST whose first element is a symbol

  (first '(+ 1 2))   ;; => +
  (rest  '(+ 1 2))   ;; => (1 2)
  (count '(+ 1 2))   ;; => 3

  ;; And we can go the other way — build code with list, run it:
  (eval (list + 1 2))
  ;; => 3

  ;; There is no separate AST. The syntax tree IS the syntax.
  )

;; -----------------------------------------------------------------
;; 2. Warm-up macro: `unless`
;;    Why can't this be a function? Because a function's arguments
;;    are ALWAYS evaluated first. A macro controls evaluation.
;; -----------------------------------------------------------------

(defmacro unless
  "Like if, but inverted. (unless test then else)"
  [test then else]
  (list 'if test else then))

(comment

  (unless false :a :b)   ;; => :a
  (unless true  :a :b)   ;; => :b

  ;; Proof that it just rewrites code before evaluation:
  (macroexpand-1 '(unless false :a :b))
  ;; => (if false :b :a)

  ;; The function version can't short-circuit:
  (defn unless-fn [test then else] (if test else then))
  (unless    false :ok (println "side effect!"))   ;; prints nothing
  (unless-fn false :ok (println "side effect!"))   ;; prints!
  )

;; -----------------------------------------------------------------
;; 3. The main event: build the threading macro ourselves
;; -----------------------------------------------------------------
;; Goal:  (my-> 5 inc (* 2) str)
;;   ==>  (str (* (inc 5) 2))
;;
;; A macro is just a function from code (data) to code (data).
;; So we can write it with... reduce.

(defmacro my->
  "Thread x through forms, inserting it as the FIRST argument."
  [x & forms]
  (reduce (fn [acc form]
            (if (seq? form)
              (cons (first form) (cons acc (rest form))) ; (f acc args...)
              (list form acc)))                          ; (f acc)
          x
          forms))

(comment

  (my-> 5 inc (* 2) str)
  ;; => "12"

  ;; Watch the rewriting happen — this is the money shot:
  (macroexpand-1 '(my-> 5 inc (* 2) str))
  ;; => (str (* (inc 5) 2))

  ;; It threads into ARBITRARY forms, first position:
  (my-> {:a 1}
        (assoc :b 2)
        (update :a inc)
        keys)
  ;; => (:a :b)

  ;; Now the reveal — the REAL one is barely different:
  (source ->)
  ;; ...a page of userland Clojure, sitting in clojure/core.clj.
  ;; So are these:
  (source when)
  (source cond)
  ;; Most of Clojure is written in Clojure.
  )

;; -----------------------------------------------------------------
;; 4. Beyond function application: some-> controls EVALUATION
;; -----------------------------------------------------------------
;; F#'s |> is definable in one line — because it's function
;; application:   let (|>) x f = f x
;; But no function/operator can decide NOT to evaluate its argument.
;; some-> stops the pipeline at the first nil (think ?. in TS/C#,
;; but userland, and for whole pipelines):

(comment

  (some-> {:user {:address {:zip "0150"}}}
          :user :address :zip clojure.string/upper-case)
  ;; => "0150"

  (some-> {:user {}}
          :user :address :zip clojure.string/upper-case)
  ;; => nil          (never calls upper-case — no NPE)

  (source some->)
  )

;; -----------------------------------------------------------------
;; 5. (Bonus, if time allows) our own nil-safe threading
;; -----------------------------------------------------------------

(defmacro my-some->
  [x & forms]
  (if (empty? forms)
    x
    (let [[form & more] forms
          g (gensym "v")]           ; fresh symbol: no variable capture
      `(let [~g ~x]
         (when (some? ~g)
           (my-some-> ~(if (seq? form)
                         (cons (first form) (cons g (rest form)))
                         (list form g))
                      ~@more))))))

(comment

  (my-some-> {:a {:b 1}} :a :b inc)   ;; => 2
  (my-some-> {:a nil}    :a :b inc)   ;; => nil, no explosion
  
  (pprint/pprint (macroexpand-all '(my-some-> {:a {:b 1}} :a :b inc)))
  )

;; -----------------------------------------------------------------
;; Closing line for the section:
;;   async/await needed a compiler release in C#, JS and (via Loom)
;;   the JVM. core.async added the same idea to Clojure as a LIBRARY
;;   — go blocks are one big macro over the code you wrote.
;; -----------------------------------------------------------------
