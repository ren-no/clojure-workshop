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
  '(+ 1 2)                 ; a LIST: symbol +, then 1, then 2

  (first '(+ 1 2))
  (rest  '(+ 1 2))
  (count '(+ 1 2))

  (eval (list + 1 2))      ; and back the other way

  ;; No separate AST. The syntax tree IS the syntax.
  )

;; -----------------------------------------------------------------
;; 2. Warm-up macro: `unless`
;; -----------------------------------------------------------------
;; A function's arguments are ALWAYS evaluated first. A macro's aren't.

(defmacro unless
  "Like if, but inverted."
  [test then else]
  (list 'if test else then))

(comment

  (unless false :a :b)
  (unless true  :a :b)

  (macroexpand-1 '(unless false :a :b))    ; just rewritten code

  ;; The function version can't short-circuit:
  (defn unless-fn [test then else] (if test else then))
  (unless    false :ok (println "side effect!"))
  (unless-fn false :ok (println "side effect!"))
  )

;; -----------------------------------------------------------------
;; 3. A feature they know by name: try-with-resources
;; -----------------------------------------------------------------
;; Java 7 shipped it as a LANGUAGE change: new grammar, AutoCloseable,
;; a compiler release (JSR 334). C# needed `using`. Here it's six lines.
;;
;;   try (var r = open()) {          (try-with [r (open)]
;;     use(r);                         (use r))
;;   }

;; Prints when closed, so the demo is visible.
(defn fake-resource [nm]
  (reify java.io.Closeable
    (close [_] (println "  closed:" nm))))

(defmacro try-with
  "Bind sym to a resource, run body, close it no matter what."
  [[sym init] & body]
  `(let [~sym ~init]
     (try
       ~@body
       (finally (.close ~sym)))))

(comment

  (macroexpand-1 '(try-with [r (fake-resource "db")] (query r)))

  (try-with [r (fake-resource "db")]
            (Thread/sleep 1000)
            (println "  using it")
            :result)

  ;; finally still runs on the way out — the guarantee Java gives:
  (try
    (try-with [r (fake-resource "db")]
              (throw (ex-info "boom" {})))
    (catch Exception e (ex-message e)))

  ;; Core already has it — a macro, not a keyword. Recursion over
  ;; `bindings` is all ours lacks: many resources, closed in reverse.
  (source with-open)

  (with-open [a (fake-resource "a")
              b (fake-resource "b")]
    :done)

  ;; `.close` was never special. Same shape around a timer:
  )

(defmacro with-timing [label & body]
  `(let [start# (System/nanoTime)]                 ; start# — auto-gensym
     (try
       ~@body
       (finally
         (println (format "%s took %.2f ms" ~label
                          (/ (- (System/nanoTime) start#) 1e6)))))))

(comment

  (with-timing "sum" (reduce + (range 1e6)))
  )

;; If asked "couldn't that be a function?" — yes. Take the body as a
;; thunk and it works, several resources and all. unless, try-with and
;; with-timing are all deferral, and thunks buy deferral back; the
;; macro just saves you writing them. Section 4 is the other kind.

;; -----------------------------------------------------------------
;; 4. The main event: build the threading macro ourselves
;; -----------------------------------------------------------------
;; Goal:  (my-> 5 inc (* 2) str)  ==>  (str (* (inc 5) 2))
;;
;; A macro is a function from code to code. So: reduce.
;; This rewrites the source form — no function can reach that.

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

  (macroexpand-1 '(my-> 5 inc (* 2) str))   ; the money shot

  (my-> {:a 1}                              ; threads into arbitrary forms
        (assoc :b 2)
        (update :a inc)
        keys)

  ;; Same move as with-open, one level up — the real one is barely
  ;; different. So are these. Most of Clojure is written in Clojure.
  (source ->)
  (source when)
  (source cond)
  )

;; -----------------------------------------------------------------
;; 5. Beyond function application: some-> controls EVALUATION
;; -----------------------------------------------------------------
;; F#'s |> is one line BECAUSE it's application:  let (|>) x f = f x
;; some-> stops at the first nil — ?. in TS/C#, but userland, and for
;; whole pipelines.

(comment

  (some-> {:user {:address {:zip "0150"}}}
          :user :address :zip clojure.string/upper-case)

  (some-> {:user {}}                        ; never calls upper-case
          :user :address :zip clojure.string/upper-case)

  (source some->)
  )

;; -----------------------------------------------------------------
;; 6. (Bonus, if time allows) our own nil-safe threading
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

  (my-some-> {:a {:b 1}} :a :b inc)
  (my-some-> {:a nil}    :a :b inc)         ; nil, no explosion

  (pprint/pprint (macroexpand-all '(my-some-> {:a {:b 1}} :a :b inc)))
  )

;; -----------------------------------------------------------------
;; Closing line for the section:
;;   async/await needed a compiler release in C#, JS and (via Loom)
;;   the JVM. core.async added the same idea to Clojure as a LIBRARY
;;   — go blocks are one big macro over the code you wrote.
;; -----------------------------------------------------------------
