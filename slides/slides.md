---
theme: default
title: The other functional programming — Clojure
colorSchema: light
transition: slide-left
lineNumbers: false
fonts:
  sans: IBM Plex Sans
  mono: IBM Plex Mono
  weights: '300,400,500,600'
  italic: true
class: parens
---

<!-- ============ BLOCK 1 · CONCEPTS (0:00–0:25) ============ -->

<div class="eyebrow">Fagdag · August 2026</div>

# Clojure

<p>
Value-oriented functional programming with Lisp on the JVM.
</p>


<!--
No agenda slide — the hook is the agenda. Tone: exploration, not evangelism. One line of framing, then straight to slide 2.
-->

---

<div class="eyebrow">THE HOOK</div>

## The console.log that lies

```js
const config = { retries: 3, timeout: 3000 };

console.log(config);       // log the "before" state

// ...somewhere far away, months later:
config.timeout = 60000;    // "just a tweak"
```

<p class="muted">Now expand the logged object in DevTools:</p>

```text
▸ { retries: 3, timeout: 60000 }     ?!  the log shows the future
```

<!--
Demo live: paste examples/console_lies.js into a BROWSER console — not Node (Node serializes at log time; the lie is DevTools lazy expansion; Chrome even admits it with the ⓘ "evaluated just now" tooltip). Everyone in the room has been bitten by this. One line to land the beat — even your debugger can't tell you what your data WAS, because it isn't a value, it's a place — then straight on. Don't open the "how do we defend" discussion here; the next slide generalizes the pain and carries the question.
-->

---

<div class="eyebrow">THE HOOK</div>

## The healthcheck that hangs

```java
var config = new RequestConfig(3, 3000);   // retries, timeout

client.fetchDashboard(config);    // slow endpoint — works fine

client.fetchHealthcheck(config);  // ?! now takes 60 s to fail
```

<p class="muted">Buried in <code>fetchDashboard</code>: <code>config.setTimeout(60000)</code> — the signature never said a word.</p>

How do we defend against these problems? <span class="muted">…and what if this bug category couldn’t exist?</span>

<!--
Slide 2 was JS, this one is Java on purpose — same disease, second language, so nobody files it under "JS footguns". Say it: C#, Kotlin, Python read identically. Deliberately no implementation shown — that IS the point: passing an object hands out write access, and neither the name nor the types warn you. Now open it to the room: defensive copies at every boundary, unmodifiable wrappers (shallow, and they throw at runtime instead of preventing), records (only if EVERYTHING nested is a record too), discipline and code review. Every answer is a tax. Close with the italic question — that's the promise of the talk.
-->

---

<div class="eyebrow">ORIENTATION</div>

## Two traditions

<svg viewBox="0 0 900 200" style="width:100%; margin-bottom:0.4em;">
  <g style="font-size:17px" fill="#142B2F">
    <g fill="#60757A">
      <rect x="35" y="8" width="230" height="38" rx="8" fill="none" stroke="#D8E5E7" stroke-width="1.5"/>
      <text x="150" y="32" text-anchor="middle">Turing machine (1936)</text>
      <line x1="150" y1="46" x2="150" y2="82" stroke="#D8E5E7" stroke-width="1.5"/>
      <rect x="35" y="86" width="230" height="38" rx="8" fill="none" stroke="#D8E5E7" stroke-width="1.5"/>
      <text x="150" y="110" text-anchor="middle">Fortran (1957) — Backus</text>
      <line x1="150" y1="124" x2="150" y2="148" stroke="#D8E5E7" stroke-width="1.5"/>
      <text x="150" y="176" text-anchor="middle">C · Java · C# · Python · JS</text>
    </g>
    <line x1="300" y1="10" x2="300" y2="192" stroke="#D8E5E7" stroke-width="1.5" stroke-dasharray="3 7"/>
    <rect x="465" y="8" width="270" height="38" rx="8" fill="none" stroke="#BECACD" stroke-width="1.5"/>
    <text x="600" y="32" text-anchor="middle">λ-calculus (1930s) — Church</text>
    <line x1="550" y1="46" x2="450" y2="82" stroke="#19547D" stroke-width="1.5"/>
    <line x1="650" y1="46" x2="750" y2="82" stroke="#017E5B" stroke-width="1.5"/>
    <rect x="345" y="86" width="210" height="38" rx="8" fill="none" stroke="#19547D" stroke-width="1.5"/>
    <text x="450" y="110" text-anchor="middle">ML (1973) — Milner</text>
    <rect x="640" y="86" width="220" height="38" rx="8" fill="none" stroke="#017E5B" stroke-width="1.5"/>
    <text x="750" y="110" text-anchor="middle">Lisp (1958) — McCarthy</text>
    <line x1="450" y1="124" x2="450" y2="148" stroke="#19547D" stroke-width="1.5"/>
    <line x1="750" y1="124" x2="750" y2="148" stroke="#017E5B" stroke-width="1.5"/>
    <text x="450" y="176" text-anchor="middle" fill="#19547D">OCaml → F# · Haskell · Scala</text>
    <text x="750" y="176" text-anchor="middle" fill="#017E5B">Scheme · Common Lisp · Clojure</text>
  </g>
</svg>

<div class="cols traditions">
  <div class="dim">
    <h3>State as places</h3>
    <p class="detail">assignment · aliasing · defensive copies</p>
    <p class="motto">The default we all inherited</p>
  </div>
  <div>
    <h3 class="blue">Types as proofs</h3>
    <p class="detail">inference · ADTs · exhaustive matching</p>
    <p class="motto">Make illegal states unrepresentable</p>
  </div>
  <div>
    <h3 class="green">Code as data</h3>
    <p class="detail">macros — the language extensible from within</p>
    <p class="motto">Programs as malleable as data</p>
  </div>
</div>

<!--
Open with the fork: 1930s, before any computer exists, Church boils computation down to one move — define a function, apply a function. That's the whole language, and it's provably as powerful as Turing's machine (Church–Turing). Two equal roots for computing: Turing's — a machine overwriting memory, step by step — and Church's — evaluating an expression down to a value. The gray branch is the one everyone in the room grew up in; mutation isn't "how programming is", it's one of two traditions.
What both λ branches inherit: functions are values (higher-order functions fall out for free), everything is an expression, closures, recursion instead of loops.
Say it explicitly: "F# folks — you already know the blue column. Today is a guided tour of the green one." Mention Hindley–Milner verbally as the historical root; don't over-claim it for Scala (local inference, different type system).
If asked: Lisp took the λ notation more than the theory — dynamic scope and broken closures until Scheme (1975) made a Lisp faithful to the calculus. And Backus, father of Fortran, used his 1977 Turing Award lecture to argue for abandoning his own branch: "Can Programming Be Liberated from the von Neumann Style?"
JS (if someone objects — slide 2 was JS): the gray row sorts by default semantics, not ancestry. Eich was hired in 1995 to put Scheme in the browser; management demanded Java's syntax. The closures survived, the defaults didn't — objects are places you overwrite. Stronger point: every gray language has spent two decades importing λ features (Java 8 lambdas, C# LINQ, C++11 lambdas). Features crossed the tree; defaults never did. That's the wedge for this talk — Clojure flips the default.
-->

---

<div class="eyebrow">ORIENTATION · A REVISIONIST LISP</div>

## Clojure learned from <span class="green">both traditions</span>

<p class="muted">A Lisp at heart — revised with lessons from the other side.</p>

<!--
The segue slide — and it defuses tribal readings: Clojure is not "team dynamic vs team static", it's a synthesis with a strong opinion about values. The next three slides are the receipts: immutability, the borrowed research, hosting as a principle.
-->

---

<div class="eyebrow">ORIENTATION · A REVISIONIST LISP</div>

## <span class="green">Immutable</span> by default

<p class="muted">Persistent data structures — mapped out in ML-family research (Okasaki).<br>
Identity as a <em>ref</em> to immutable values — inspired by SML’s <code>ref</code>.</p>

<!--
Beat 1: the heresy — a Lisp that refuses mutation. Say orally: classic Lisps mutate freely, so this is Hickey's break with his own family. Credit precisely: Okasaki's "Purely Functional Data Structures" (1998) mapped the persistent-structure space; Hickey's identity/state split ("Clojure's Approach to Identity and State") cites SML's ref as the inspiration for indirect references to immutable data. The concrete HAMT implementation is Bagwell's Ideal Hash Trees (EPFL) made persistent by Hickey — not ML research per se. Pays off later on the structural-sharing slide.
-->

---

<div class="eyebrow">ORIENTATION · A REVISIONIST LISP</div>

## Lazy evaluation

<p class="muted">Inspired by Haskell — but just sequences, not everything.</p>

<!--
Beat 2: laziness where it helps. Sequences can be infinite, production separated from consumption — but unlike Haskell, evaluation is otherwise eager and predictable.
-->

---

<div class="eyebrow">ORIENTATION · A REVISIONIST LISP</div>

## Hosted, <span class="green">by design</span>

<p class="muted">No private island: Clojure targets the JVM · ClojureScript, JavaScript · ClojureCLR, .NET.<br>
Host types are Clojure’s types — interop is a language feature, not an FFI.</p>

<!--
Beat 3: pragmatism as a design decision. Hickey's rationale: a new language island forfeits decades of libraries, GC and JIT work — hosting inherits them on day one. Strings ARE java.lang.String / JS strings; no marshalling layer. The triple lands per audience: JVM for the Javaists, JS for the TypeScripters, CLR for the F# crowd. Keep it at the principle here — the practical "keep your profilers, deploy a jar, npm" payoff belongs to the adoption slide near the end.
-->

---

<div class="eyebrow">ORIENTATION</div>

## Two superpowers — honestly

<div class="cols" style="margin-top:2.5em;">
  <div>
    <div class="power-label">ML</div>
    <p class="power blue">Exhaustive matching</p>
    <p class="muted">The compiler catches the case you forgot.</p>
  </div>
  <div>
    <div class="power-label">LISP</div>
    <p class="power green">Everything is data</p>
    <p class="muted">Maps, vectors — and the program itself.</p>
  </div>
</div>

<p style="margin-top:2.5em;">Today’s talk is a tour of the <span class="green">second superpower</span>.</p>

<!--
The credibility slide — say the trade-offs OUT LOUD, in both directions: Clojure will NOT give you exhaustiveness checking; typed languages can't treat the program as data without leaving the language (T4, source generators, Template Haskell…). "Everything is data" is the through-line of the talk: values and maps now, the program itself in act two — that's what makes macros possible. Foreshadows 0:50 so it lands as a payoff, not a pivot.
-->

---

<div class="eyebrow">VALUES · after Rich Hickey</div>

## Place-oriented programming

<p class="muted">A variable or object property is a <em>place</em>.<br>
A new value obliterates the old one.<br>
A kilobytes-of-RAM constraint that never left.</p>

<p style="margin-top:1.4em;">The lying log isn’t a bug — it’s the paradigm <span class="green">working as designed</span>.</p>

<!--
Hickey core: "The Value of Values". Say the arc verbally: a place = a memory address; overwriting made sense when RAM was measured in kilobytes; the constraint leaked into our program SEMANTICS and never left. Frame it as history, not blame — every language in the room inherited it. Kicker: the lying log and the hanging healthcheck are not coding errors — mutation at a distance is the paradigm doing exactly what it promises.
-->

---

<div class="eyebrow">VALUES · after Rich Hickey</div>

## Value-oriented programming

<p class="muted">a fact at a point in time<br>
accrete don't overwrite<br>
</p>

<p>
Values account for <em>time</em>; places erase it.
</p>

<p style="margin-top:1.4em;">You already trust this: <span class="green">ledgers, medical journals, git</span>.<br>
Your ints and strings already work this way.<br>
<span class="green">In Clojure, everything does.</span></p>

<!--
"The Value of Values" in one slide. A value is immutable, semantically transparent, freely shareable, comparable. The TIME argument is the core: a place holds only the latest write — history is gone; with values, old and new coexist, so time becomes explicit in the program. Hickey's line: "accountants don't use erasers" — double-entry ledgers, medical journals, git commits are value-oriented systems everyone already trusts. Second hook: the room already programs with values daily — nobody defensively copies an int, nobody fears their 42 becoming 43 behind their back or a string mutating mid-print (java.util.Date is the exception that proves the rule). Clojure's move isn't exotic: it just extends the scalar guarantee to collections. The next slides make it concrete: equality, updates, structural sharing.
-->

---

<div class="eyebrow">SYNTAX · A 30-SECOND PRIMER</div>

## How to read Clojure

<div class="cols" style="margin-top:1em;">
<div>

```js
// JavaScript
add(1, 2)
```

</div>
<div>

```clojure
;; Clojure — the verb moves inside
(add 1 2)
```

</div>
</div>

<div class="cols">
<div>

```clojure
:timeout       ; keyword
{:retries 3}   ; map
[1 2 3]        ; vector
#{"web-1"}     ; set
(add 1 2)      ; list — a call!
```

</div>
<div>

```clojure
;; defining a function — params are a vector
(defn add [x y]
  (+ x y))

(add 1 2)      ; => 3
```

</div>
</div>

<p style="margin-top:1em;">Data literals + parentheses — <span class="green">that’s the whole syntax</span>.</p>

<!--
30 seconds, not a lesson. The only move: the function name goes INSIDE the parens, first position. Everything else on screen is a data literal — and these exact literals carry the rest of the talk. No operators, no precedence, no statements-vs-expressions.
-->

---

<div class="eyebrow">VALUES</div>

## Identity vs equality — a false dilemma

<div class="cols">
<div>

```js
// JavaScript: compares places
{a: 1} === {a: 1}         // false
[1, 2, 3] === [1, 2, 3]   // false
```

```java
// Java: places too — even .equals
var p1 = new Point(1, 2);
p1.equals(new Point(1, 2)) // false!
// value equality: hand-write equals()
// + hashCode() — for every class
```

</div>
<div v-click>

```clojure
;; Clojure: equality IS identity
(= {:a 1} {:a 1})          ; true
(identical? {:a 1} {:a 1}) ; false — and nobody cares
```

```clojure
;; Maps can even be KEYS in maps…
{{:method :get
  :path "/tickets"} list-tickets}
;; …and live in sets, safely
#{{:host "web-1"} {:host "web-2"}}
```

</div>
</div>

<!--
The "wait, what?" slide. Left column is the place-oriented world: JS === on structures, Java Object.equals defaulting to reference identity — value equality means hand-writing equals/hashCode per class (records fix this for data carriers; C# is the same story, records opt in). Right column: the identity/equality distinction is MANUFACTURED BY MUTATION (Baker's "egal", "Equal Rights for Functional Objects" 1993 — the paper behind Clojure's =). Pointer identity only matters because equal mutable objects can diverge later; for immutable values the distinction collapses — Clojure has identical? and idiomatic code never uses it. Bonus beats: = works across collection types — (= '(1 2 3) [1 2 3]) is true; values live safely in sets and as map keys, while mutable HashMap keys on the JVM are a classic hazard (mutate → lose the entry).
-->

---

<div class="eyebrow">VALUES</div>

## “Updates” return new values

```clojure
(def config {:retries 3 :timeout 5000})

(def batch-config (assoc config :timeout 60000))

batch-config  ; => {:retries 3, :timeout 60000}
config        ; => {:retries 3, :timeout 5000}   ← untouched. Always.
```

<div class="cols" style="margin-top:1.8em;">
  <div>
    <div class="power-label">HISTORY</div>
    <p class="power">Free</p>
    <p class="muted">Old and new coexist — diffing is trivial.</p>
  </div>
  <div>
    <div class="power-label">SHARING</div>
    <p class="power">Fearless</p>
    <p class="muted">Nothing you hold can change under you.</p>
  </div>
  <div>
    <div class="power-label">MUTATION AT A DISTANCE</div>
    <p class="power green">Impossible</p>
    <p class="muted">No surprise mutations, no surprise state.</p>
  </div>
</div>

<!--
Callback to the hook (slides 2–3) — close the loop explicitly. Free · Fearless · Impossible: land each word, then the payoff — "The snapshot cannot lie, because there is nothing anyone can do to it."
-->

---

<div class="eyebrow">VALUES</div>

## “Isn’t that slow?” — structural sharing

<div class="cols">
<div>

<svg viewBox="0 0 460 260" style="width:100%;">
  <defs>
    <marker id="share-arrow" markerUnits="userSpaceOnUse" markerWidth="8" markerHeight="8" refX="1" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 Z" fill="#017E5B"/>
    </marker>
  </defs>
  <g stroke-width="1.5" style="font-size:15px">
    <!-- v: the old tree, never touched -->
    <line x1="130" y1="40" x2="72"  y2="122" stroke="#BECACD"/>
    <line x1="130" y1="40" x2="188" y2="122" stroke="#BECACD"/>
    <line x1="72"  y1="122" x2="42"  y2="212" stroke="#BECACD"/>
    <line x1="72"  y1="122" x2="102" y2="212" stroke="#BECACD"/>
    <line x1="188" y1="122" x2="158" y2="212" stroke="#BECACD"/>
    <line x1="188" y1="122" x2="218" y2="212" stroke="#BECACD"/>
    <circle cx="130" cy="40"  r="17" fill="#fff" stroke="#60757A"/>
    <circle cx="72"  cy="122" r="17" fill="#fff" stroke="#60757A"/>
    <circle cx="188" cy="122" r="17" fill="#fff" stroke="#60757A"/>
    <circle cx="42"  cy="212" r="17" fill="#fff" stroke="#60757A"/>
    <circle cx="102" cy="212" r="17" fill="#fff" stroke="#60757A"/>
    <circle cx="158" cy="212" r="17" fill="#fff" stroke="#60757A"/>
    <circle cx="218" cy="212" r="17" fill="#fff" stroke="#60757A"/>
    <text x="130" y="14" text-anchor="middle" fill="#60757A">v</text>
    <text x="42"  y="217" text-anchor="middle" fill="#60757A">a</text>
    <text x="102" y="217" text-anchor="middle" fill="#60757A">b</text>
    <text x="158" y="217" text-anchor="middle" fill="#60757A">c</text>
    <text x="218" y="217" text-anchor="middle" fill="#60757A">d</text>
    <text x="42"  y="248" text-anchor="middle" fill="#BECACD" style="font-size:10px">0</text>
    <text x="102" y="248" text-anchor="middle" fill="#BECACD" style="font-size:10px">1</text>
    <text x="158" y="248" text-anchor="middle" fill="#BECACD" style="font-size:10px">2</text>
    <text x="218" y="248" text-anchor="middle" fill="#60757A" style="font-size:10px">3</text>
    <!-- v′: one new root→leaf path; dashed arrows = pointers back into v -->
    <g v-click>
      <line x1="330" y1="40"  x2="330" y2="122" stroke="#017E5B"/>
      <line x1="330" y1="122" x2="352" y2="212" stroke="#017E5B"/>
      <line x1="330" y1="40"  x2="95"  y2="115" stroke="#017E5B" stroke-dasharray="5 4" marker-end="url(#share-arrow)"/>
      <line x1="330" y1="122" x2="179" y2="201" stroke="#017E5B" stroke-dasharray="5 4" marker-end="url(#share-arrow)"/>
      <circle cx="330" cy="40"  r="17" fill="#D1F7D2" stroke="#017E5B"/>
      <circle cx="330" cy="122" r="17" fill="#D1F7D2" stroke="#017E5B"/>
      <circle cx="352" cy="212" r="17" fill="#D1F7D2" stroke="#017E5B"/>
      <text x="330" y="14" text-anchor="middle" fill="#017E5B">v′ = (assoc v 3 x)</text>
      <text x="352" y="217" text-anchor="middle" fill="#017E5B">x</text>
      <text x="352" y="248" text-anchor="middle" fill="#017E5B" style="font-size:10px">3</text>
    </g>
  </g>
</svg>

<p v-after class="muted small">Three new nodes, total. The dashed arrows point back into v — everything else is shared.</p>

</div>
<div>

- Persistent vectors are wide trees <span class="muted">(32-way tries)</span>
- `assoc` copies ~log₃₂ n small nodes and **shares the rest**
- 1M-element vector: full copy moves 10⁶ refs, `assoc` copies ~10² — **four orders of magnitude less work**

</div>
</div>

<!--
The slide the math/EE crowd enjoys most — immutability goes from virtue to engineering solution. Beat: v alone first ("we're about to assoc at slot 3"), one click drops in v′ — three green nodes, arrows into everything reused. log₃₂(10⁹) ≈ 6: effectively constant. Orally: on the wall clock that's ≈40 ms vs ≈0.01 ms for 1M elements — you'll prove the timings live in session 1.
-->

---

<div class="eyebrow">VALUES</div>

## The industry agrees

<div style="margin-top:2.2em;">
<div class="power-label">PLACES BY DEFAULT · VALUES OPT-IN</div>
<p class="power dim">Java · C# · TypeScript</p>
<p class="muted">records · <code>List.of</code> · <code>readonly</code> · <code>as const</code> · Immer</p>
</div>

<div v-click style="margin-top:1.8em;">
<div class="power-label">VALUES BY DEFAULT</div>
<p class="power">Haskell · F# · <span class="green">Clojure</span></p>
<p class="muted">In Clojure it goes all the way down — the entire standard library assumes it.</p>
</div>

<!--
Generous framing, no dunking — but two distinct tiers. Say out loud: Java and C# are genuinely moving this way (records, value classes on the roadmap), yet the DEFAULT is still places — values are something you opt into. Haskell is the strictest of the bunch — purity enforced by the type system, mutation quarantined in IO/ST. F# flips the default: records and unions are immutable unless you say otherwise (though it still sits on the mutable .NET base library). Clojure starts at values for everything — every core data structure, the whole stdlib, the ecosystem's idioms. That's the difference between a feature and a paradigm. Beat: quiet tier first ("all your languages are adding this…"), one click ("…and these just start there").
-->

---
class: parens
---

<div class="eyebrow">BRIDGE</div>

<p class="quote">“It is better to have 100 functions operate on one data structure than 10 functions on 10 data structures.”
<span class="attribution">— Alan Perlis, epigram #9</span></p>

<span class="switch">→ to the REPL · sessions: values, data-oriented</span>

<!--
SWITCH 1 → editor. This slide stays parked if anyone glances back at the projector notes. Sessions: workshop/values.clj then workshop/data_oriented.clj (~25 min total). Evaluate, don't type.
-->

---

<!-- ============ BLOCK 2 · ONE SLIDE (0:50) ============ -->

<div class="eyebrow">ACT TWO</div>

## Code is data

```clojure
(+ 1 2)
```

<p style="margin-top:0.8em;">This is not text that <em>looks like</em> a list.<br>
It <strong>is</strong> a list — first element <code>+</code>, then <code>1</code>, then <code>2</code>.</p>

<p class="muted">There is no separate AST. The syntax tree <em>is</em> the syntax.<br>That property is called <strong>homoiconicity</strong> — let’s use it.</p>

<span class="switch">→ to the REPL · session: macros</span>

<!--
SWITCH 2 → deck (this one slide, a breather after 25 min of demo), then SWITCH 3 → editor for workshop/macros.clj: first/eval, unless, build my->, macroexpand-1, (source ->). ~15–20 min.
-->

---

<!-- ============ BLOCK 3 · LANDING (1:10–1:20) ============ -->

<div class="eyebrow">THE THESIS</div>

## Language features are libraries

```clojure
(source ->)      ; the REAL threading macro, from clojure/core.clj
(defmacro -> [x & forms]
  (loop [x x, forms forms]
    (if forms
      (let [form (first forms)
            threaded (if (seq? form)
                       (with-meta `(~(first form) ~x ~@(next form)) (meta form))
                       (list form x))]
        (recur threaded (next forms)))
      x)))
```

- F#’s `|>` is elegant function application — `some->` controls <em>evaluation itself</em>
- `core.async` added CSP concurrency **as a library**; async/await needed compiler releases elsewhere

<!--
The thesis slide of the whole talk. You just built my-> live; here's proof the real one is a page of userland Clojure. Be precise and generous about |> — definable in one line BECAUSE it's application; macros operate one level up.
-->

---

<div class="eyebrow">IN PRACTICE</div>

## Where this bites, in practice

- **Concurrency** — immutable values + `atom`/`swap!` make shared state tractable
- **Testing** — pure functions over values need no mocks: data in, data out, `=`
- **Debugging** — values print, compare, serialize; capture a failing input <em>as literal data</em> and replay it in the REPL

<!--
Tie back to pains they have this week. If ahead of schedule: 60-second live atom demo — (def a (atom {})) (swap! a assoc :x 1) — point out swap! takes a pure function of value → value.
-->

---

<div class="eyebrow">ADOPTION</div>

## Clojure meets your stack where it is

- **Java devs** — full JVM interop: call any Java library directly, deploy as a jar, keep your profilers and ops
- **TypeScript devs** — ClojureScript compiles to JS, runs on npm, React via Reagent
- Curiosity is low-risk: a library, a script, a build tool — not a rewrite

<!--
Positions the next step as safe. Babashka is also worth a mention for the shell-scripting crowd — Clojure with instant startup.
-->

---

<div class="eyebrow">GO DEEPER</div>

## Start this afternoon

- **Talks** — Rich Hickey: <em>The Value of Values</em> · <em>Simple Made Easy</em>
- **Book** — <em>Clojure for the Brave and True</em> (free online: braveclojure.com)
- **Practice** — exercism.org/tracks/clojure · 4clojure.oxal.org
- **Zero setup** — tryclojure.org in your browser, right now
- **This repo** — slides + every REPL session you just watched

<!--
Make the next step take zero installs. Share the repo link/path here so people can replay the sessions themselves.
-->

---
class: parens
---

<div class="eyebrow">TAKK</div>

# Questions?

<p class="muted">values · code as data · language features as libraries</p>

<!--
Parks on screen during the 1:20–1:30 buffer. Don't plan content here.
-->

---

<!-- ============ APPENDIX (backup, not presented) ============ -->

<div class="eyebrow">APPENDIX A · BACKUP</div>

## macroexpand, precomputed

```clojure
(macroexpand-1 '(unless false :a :b))
;; => (if false :b :a)

(macroexpand-1 '(my-> 5 inc (* 2) str))
;; => (str (* (inc 5) 2))

(macroexpand-1 '(my-> {:a 1} (assoc :b 2) (update :a inc) keys))
;; => (keys (update (assoc {:a 1} :b 2) :a inc))

(macroexpand-1 '(my-some-> m :a :b))
;; => (clojure.core/let [v123 m]
;;      (clojure.core/when (clojure.core/some? v123)
;;        (workshop.macros/my-some-> (:a v123) :b)))
```

<!--
Insurance: if the live macro build stumbles, show these and move on. Gensym'd symbol name will differ — that's fine, say so.
-->

---

<div class="eyebrow">APPENDIX B · BACKUP</div>

## Structural sharing, with numbers

- Branching factor 32 → depth of a 10⁹-element vector: **6**
- `assoc` on 10⁶ elements: copy ~4 nodes × 32 refs ≈ a few hundred bytes
- Old and new versions coexist; GC reclaims whatever nobody references
- Same trick for maps (HAMT — hash array mapped trie, Bagwell 2001)

<!--
For the deep-divers during Q&A. Phil Bagwell's "Ideal Hash Trees" paper is the citation if someone asks.
-->
