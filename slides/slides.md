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

<div class="title-row">
<CljLogo class="title-logo" />

# Clojure

</div>

<p>
Value-oriented functional programming with Lisp
</p>

<p class="hosts">JVM · JS · CLR · native</p>


<!--
No agenda slide — the hook is the agenda. Tone: exploration, not evangelism. One line of framing, then straight to slide 2.
-->

---

<div class="eyebrow">Why?</div>

## The console.log that lies

```js
const config = { retries: 3, timeout: 3000 };

console.log(config);

config.timeout = 60000;
```

<v-click>

```text
▸ { retries: 3, timeout: 60000 }     ?!  the log shows the future
```

</v-click>

<!--
Ask the room what the log shows before clicking — let them commit to { retries: 3, timeout: 3000 }, then reveal.
Demo live: paste examples/console_lies.js into a BROWSER console — not Node (Node serializes at log time; the lie is DevTools lazy expansion; Chrome even admits it with the ⓘ "evaluated just now" tooltip). Everyone in the room has been bitten by this. One line to land the beat — even your debugger can't tell you what your data WAS, because it isn't a value, it's a place — then straight on. Don't open the "how do we defend" discussion here; the next slide generalizes the pain and carries the question.
-->

---

<div class="eyebrow">Why?</div>

## The healthcheck that hangs

```java
class Client {
  public string fetchDashboard(RequestConfig config) {
    config.setTimeout(60000);
    // Lots of code
    return this.fetch(payload, config)
  }
}

var config = new RequestConfig(3, 3000);   // retries, timeout
client.fetchDashboard(config);
client.fetchHealthcheck(config); // Woops, times out only after 60000 seconds
```

<!--
Slide 2 was JS, this one is Java on purpose — same disease, second language, so nobody files it under "JS footguns". Say it: C#, Kotlin, Python read identically. Deliberately no implementation shown — that IS the point: passing an object hands out write access, and neither the name nor the types warn you. Now open it to the room: defensive copies at every boundary, unmodifiable wrappers (shallow, and they throw at runtime instead of preventing), records (only if EVERYTHING nested is a record too), discipline and code review. Every answer is a tax. Close with the italic question — that's the promise of the talk.
-->

---

<div class="eyebrow">ORIENTATION</div>

## Two traditions

<svg viewBox="0 0 900 200" style="width:100%; margin-bottom:0.4em;">
  <g style="font-size:17px" fill="#142B2F">
    <!-- Turing branch: ink on its own beats — the tape, then its own line of
         descent — quiet while λ has the floor, backdrop once λ forks.
         One class does all the receding, so nothing is dimmed twice; every
         colour in here is the normal ink. -->
    <g class="branch" :class="$clicks >= 3 ? 'branch-back' : ($clicks === 1 ? 'branch-quiet' : '')">
      <rect x="15" y="8" width="270" height="38" rx="8" fill="none" stroke="#BECACD" stroke-width="1.5"/>
      <text x="150" y="32" text-anchor="middle">Turing machine (1936) — Turing</text>
      <g v-click.hide="2" transform="translate(20, 66)">
        <TuringTape :running="$clicks < 1" />
      </g>
      <g v-click="2">
        <line x1="150" y1="46" x2="150" y2="82" stroke="#BECACD" stroke-width="1.5"/>
        <rect x="35" y="86" width="230" height="38" rx="8" fill="none" stroke="#BECACD" stroke-width="1.5"/>
        <text x="150" y="110" text-anchor="middle">Fortran (1957) — Backus</text>
        <line x1="150" y1="124" x2="150" y2="148" stroke="#BECACD" stroke-width="1.5"/>
        <text x="150" y="176" text-anchor="middle">C · Java · C# · Python · JS</text>
      </g>
    </g>
    <line x1="310" y1="10" x2="310" y2="192" stroke="#D8E5E7" stroke-width="1.5" stroke-dasharray="3 7"/>
    <!-- λ branch: on screen from the start — the room needs to see there are
         two roots — but only ink on its own beats: the factorial unfold, and
         the fork into ML and Lisp on the last click. -->
    <g class="branch" :class="($clicks === 1 || $clicks >= 3) ? '' : 'branch-quiet'">
      <rect x="465" y="8" width="270" height="38" rx="8" fill="none" stroke="#BECACD" stroke-width="1.5"/>
      <text x="600" y="32" text-anchor="middle">λ-calculus (1930s) — Church</text>
      <g v-click.hide="3" transform="translate(475, 90)">
        <LambdaFactorial :running="$clicks === 1" />
      </g>
      <g v-click="3">
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
    </g>
  </g>
</svg>

<div class="traditions traditions-stage">
  <div class="swap branch" :class="$clicks >= 3 ? 'branch-back' : ($clicks === 1 ? 'branch-quiet' : '')">
    <div v-click.hide="2">
      <div class="prims">
        <p class="prim"><strong>tape</strong> — cells of symbols</p>
        <p class="prim"><strong>head</strong> — read · write · move</p>
        <p class="prim"><strong>rules</strong> — a finite state table</p>
      </div>
      <p class="motto">Compute by overwriting memory</p>
    </div>
    <div v-click="2">
      <h3>State as places</h3>
      <p class="detail">assignment · aliasing · defensive copies</p>
      <p class="motto">The default we all inherited</p>
    </div>
  </div>
  <div class="swap span-2 branch" :class="($clicks === 1 || $clicks >= 3) ? '' : 'branch-quiet'">
    <div v-click.hide="3">
      <div class="prims">
        <p class="prim"><strong>variables</strong> — <code>x</code></p>
        <p class="prim"><strong>abstraction</strong> — <code>λx.e</code> — define a function</p>
        <p class="prim"><strong>application</strong> — <code>f x</code> — apply a function</p>
      </div>
      <p class="motto">Compute by evaluating expressions</p>
    </div>
    <div class="cols-2" v-click="3">
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
  </div>
</div>

<!--
Beat 0 — one formalism at a time. The λ side is on screen but ghosted, so don't talk about it yet; it's there only to promise a second root. The tape machine runs while you talk Turing: a tape of cells, a head that reads/writes/moves, a finite rule table — compute by overwriting memory, step by step (the animation is literally doing it: walk, write a 1, walk back, erase).
Click 1 — the two sides trade places: the tape parks and recedes to a ghost, the λ side comes up to full ink and its factorial starts unfolding. Say "same power, opposite default move" on the handover. Three constructs, nothing else: three constructs, nothing else — a variable, defining a function, applying a function. The factorial unfold shows the third trick: no loops, a function applying itself — recursion. Provably equal in power (Church–Turing): two equal roots, opposite default moves.
Click 2 — the Turing side comes back to full ink and cashes out alone: Fortran, then C, Java, C#, Python, JS — everything the room grew up in, one line of descent from a tape. Land the reframe here, with nothing on the right competing: mutation isn't "how programming is", it's one of two traditions.
Click 3 — the λ root forks into two schools and takes the ink; the imperative line drops to a backdrop but stays readable, because the contrast IS the slide. What both λ branches inherit: functions are values (higher-order functions fall out for free), everything is an expression, closures, recursion instead of loops.
Say it explicitly: "F# folks — you already know the blue column. Today is a guided tour of the green one." Mention Hindley–Milner verbally as the historical root; don't over-claim it for Scala (local inference, different type system).
If asked: Lisp took the λ notation more than the theory — dynamic scope and broken closures until Scheme (1975) made a Lisp faithful to the calculus. And Backus, father of Fortran, used his 1977 Turing Award lecture to argue for abandoning his own branch: "Can Programming Be Liberated from the von Neumann Style?"
JS (if someone objects — slide 2 was JS): the gray row sorts by default semantics, not ancestry. Eich was hired in 1995 to put Scheme in the browser; management demanded Java's syntax. The closures survived, the defaults didn't — objects are places you overwrite. Stronger point: every gray language has spent two decades importing λ features (Java 8 lambdas, C# LINQ, C++11 lambdas). Features crossed the tree; defaults never did. That's the wedge for this talk — Clojure flips the default.
-->

---

<div class="eyebrow">ORIENTATION · A REVISIONIST LISP</div>

## Clojure learned from <span class="green">both λ-traditions</span>

<p class="muted">A Lisp at heart — revised with lessons from the ML side.</p>

<!--
The segue slide — and it defuses tribal readings: Clojure is not "team dynamic vs team static", it's a synthesis with a strong opinion about values. The next three slides are the receipts: immutability, the borrowed research, hosting as a principle.
-->

---

<div class="eyebrow">ORIENTATION · A REVISIONIST LISP</div>

## <span class="green">Immutable</span> by default

<p class="muted">Persistent data structures — Okasaki<br>
Identity as a <em>ref</em> to immutable values — SML’s <code>ref</code>.</p>

<!--
Beat 1: the heresy — a Lisp that refuses mutation. Say orally: classic Lisps mutate freely, so this is Hickey's break with his own family. Credit precisely: Okasaki's "Purely Functional Data Structures" (1998) mapped the persistent-structure space; Hickey's identity/state split ("Clojure's Approach to Identity and State") cites SML's ref as the inspiration for indirect references to immutable data. The concrete HAMT implementation is Bagwell's Ideal Hash Trees (EPFL) made persistent by Hickey — not ML research per se. Pays off later on the structural-sharing slide.
-->

---

<div class="eyebrow">ORIENTATION · A REVISIONIST LISP</div>

## <span class="green">Lazy</span> evaluation

<p class="muted">Inspired by Haskell — but just sequences</p>

<!--
Beat 2: laziness where it helps. Sequences can be infinite, production separated from consumption — but unlike Haskell, evaluation is otherwise eager and predictable.
-->

---

<div class="eyebrow">ORIENTATION · A REVISIONIST LISP</div>

## <span class="green">Hosted</span>, by design

<div class="cols" style="margin-top:2.2em;">
  <div>
    <div class="power-label">Clojure</div>
    <p class="power">JVM</p>
  </div>
  <div>
    <div class="power-label">ClojureScript</div>
    <p class="power">JavaScript</p>
  </div>
  <div>
    <div class="power-label">ClojureCLR</div>
    <p class="power">.NET</p>
  </div>
</div>

<!-- <p class="muted" style="margin-top:2.2em;">Host types are Clojure’s types — interop is a language feature, not an FFI.</p> -->

<!--
Beat 3: pragmatism as a design decision. Open with the phrase "no private island" out loud — it came off the slide. Hickey's rationale: a new language island forfeits decades of libraries, GC and JIT work — hosting inherits them on day one. Strings ARE java.lang.String / JS strings; no marshalling layer. The triple lands per audience: JVM for the Javaists, JS for the TypeScripters, CLR for the F# crowd. Keep it at the principle here — the practical "keep your profilers, deploy a jar, npm" payoff belongs to the adoption slide near the end.
-->

---

<div class="eyebrow">ORIENTATION</div>

## Two superpowers — honestly

<div class="cols" style="margin-top:2.5em;">
  <div>
    <div class="power-label">ML</div>
    <p class="power blue">Exhaustive matching</p>
  </div>
  <div>
    <div class="power-label">LISP</div>
    <p class="power green">Everything is data</p>
  </div>
</div>
<!--
The credibility slide — say the trade-offs OUT LOUD, in both directions: Clojure will NOT give you exhaustiveness checking; typed languages can't treat the program as data without leaving the language (T4, source generators, Template Haskell…). "Everything is data" is the through-line of the talk: values and maps now, the program itself in act two — that's what makes macros possible. Foreshadows 0:50 so it lands as a payoff, not a pivot.
-->

---

<div class="eyebrow">VALUES · after Rich Hickey</div>

## <span class="blue">Place-oriented</span> programming

<p class="muted">A variable is a <em>place</em><br>
A new value obliterates the old one<br>
A kilobytes-of-RAM constraint</p>

<!--
Hickey core: "The Value of Values". Say the arc verbally: a place = a memory address; overwriting made sense when RAM was measured in kilobytes; the constraint leaked into our program SEMANTICS and never left. Frame it as history, not blame — every language in the room inherited it. Kicker: the lying log and the hanging healthcheck are not coding errors — mutation at a distance is the paradigm doing exactly what it promises.
-->

---

<div class="eyebrow">VALUES · after Rich Hickey</div>

## <span class="green">Value-oriented</span> programming

<p class="muted">a fact at a point in time<br>
accumulate don't overwrite<br>
</p>

<p>
Values account for <em>time</em>; places don't.
</p>

<p style="margin-top:1.4em;">
<span class="green">In Clojure everything's a value</span></p>

<!--
"The Value of Values" in one slide. A value is immutable, semantically transparent, freely shareable, comparable. The TIME argument is the core: a place holds only the latest write — history is gone; with values, old and new coexist, so time becomes explicit in the program. Hickey's line: "accountants don't use erasers" — double-entry ledgers, medical journals, git commits are value-oriented systems everyone already trusts. Second hook: the room already programs with values daily — nobody defensively copies an int, nobody fears their 42 becoming 43 behind their back or a string mutating mid-print (java.util.Date is the exception that proves the rule). Clojure's move isn't exotic: it just extends the scalar guarantee to collections. The next slides make it concrete: equality, updates, structural sharing.
-->

---

<div class="eyebrow">SYNTAX · A 30-SECOND PRIMER</div>

## How to read Clojure

<div class="code-swap" style="margin-top:1.2em;">

<div v-click.hide="1">

```clojure
add(1, 2)      ; JavaScript — the verb sits outside
(add 1 2)      ; Clojure — the verb moves inside
```

</div>

<div v-click="[1, 2]">

```clojure
:timeout       ; keyword
{:retries 3}   ; map
[1 2 3]        ; vector
#{"web-1"}     ; set
(add 1 2)      ; list — a function invocation!
```

</div>

<div v-click="2">

```clojure
;; defining a function — params are a vector
(defn add [x y]
  (+ x y))

(add 1 2)      ; => 3
```

</div>

</div>

<p style="margin-top:1.4em;">Data literals + parentheses — <span class="green">that’s the whole syntax</span>.</p>

<!--
30 seconds, not a lesson — three beats in ONE panel, each click replacing the last. Beat 1: the only move — the verb goes INSIDE the parens, first position; say the JS line out loud, then the Clojure one. Beat 2: the data literals, and these exact literals carry the rest of the talk — keyword, map, vector, set, and a list that is a function call. Beat 3: defn — params are a vector, the body is just another call. No operators, no precedence, no statements-vs-expressions.
-->

---

<div class="eyebrow">VALUES</div>

## Identity vs equality — a false dilemma

<div class="code-swap" style="margin-top:1.2em;">

<div v-click.hide="1">

```js
// JavaScript - true or false?
{a: 1} === {a: 1}
[1, 2, 3] === [1, 2, 3]
```

</div>

<div v-click="[1, 2]">

```java
// Java - true or false?
var p1 = new Point(1, 2);
var p2 = new Point(1, 2);
p1.equals(p2)
```

</div>

<div v-click="[2, 3]">

```clojure
;; Clojure
(= {:a 1} {:a 1})          ; true
(identical? {:a 1} {:a 1}) ; false
```

</div>

<div v-click="3">

```clojure
;; Maps can even be KEYS in maps
{{:method :get
  :path "/tickets"} list-tickets}
```

</div>

</div>

<!--
One panel, four beats — the room only ever has one thing to look at, and the results are deliberately missing so they have to call them.
Beat 1 (JavaScript): ask the room — === on two structures: false, false. Everyone knows this one.
Beat 2 (Java): and .equals(new Point(1, 2))… also false! Object.equals defaults to reference identity; value equality means hand-writing equals/hashCode per class (records fix this for data carriers; C# is the same story, records opt in). Let that land before clicking.
Beat 3: Clojure answers. The identity/equality distinction is MANUFACTURED BY MUTATION (Baker's "egal", "Equal Rights for Functional Objects" 1993 — the paper behind Clojure's =). Pointer identity only matters because equal mutable objects can diverge later; for immutable values the distinction collapses — Clojure has identical? and idiomatic code never uses it. Bonus: = works across collection types — (= '(1 2 3) [1 2 3]) is true.
Beat 4: maps as KEYS in maps — and say verbally that values also live safely in SETS — #{{:host "web-1"} {:host "web-2"}} — while mutable HashMap keys on the JVM are a classic hazard (mutate → lose the entry).
-->

---

<div class="eyebrow">VALUES</div>

## Updates return new values

<div class="beat-swap">

<div v-click.hide="1">

```clojure
(def config {:retries 3 :timeout 5000})

(def batch-config (assoc config :timeout 60000))

batch-config  ; => {:retries 3, :timeout 60000}
config        ; => {:retries 3, :timeout 5000}   ← untouched. Always.
```

</div>

<div class="verdict">
  <div v-click="1">
    <div class="power-label">HISTORY</div>
    <p class="verdict-word">Free</p>
  </div>
  <div v-click="2">
    <div class="power-label">SHARING</div>
    <p class="verdict-word">Fearless</p>
  </div>
  <div v-click="3">
    <div class="power-label">MUTATION AT A DISTANCE</div>
    <p class="verdict-word green">Impossible</p>
  </div>
</div>

</div>

<!--
Callback to the hook (slides 2–3) — close the loop explicitly. Free · Fearless · Impossible: land each word, then the payoff — "The snapshot cannot lie, because there is nothing anyone can do to it."
-->

---

<div class="eyebrow">VALUES</div>

## “Isn’t that slow?” — structural sharing

<svg class="sharing-svg" viewBox="0 0 900 350">
  <defs>
    <marker id="share-arrow" markerUnits="userSpaceOnUse" markerWidth="11" markerHeight="11" refX="1" refY="5.5" orient="auto">
      <path d="M0,0 L11,5.5 L0,11 Z" fill="#017E5B"/>
    </marker>
  </defs>
  <g stroke-width="2" style="font-size:18px">
    <!-- v: the old tree, never touched. Leaves sit wide apart on purpose —
         real persistent vectors are 32-way, not binary. -->
    <line x1="315" y1="62"  x2="175" y2="176" stroke="#BECACD"/>
    <line x1="315" y1="62"  x2="455" y2="176" stroke="#BECACD"/>
    <line x1="175" y1="176" x2="105" y2="290" stroke="#BECACD"/>
    <line x1="175" y1="176" x2="245" y2="290" stroke="#BECACD"/>
    <line x1="455" y1="176" x2="385" y2="290" stroke="#BECACD"/>
    <line x1="455" y1="176" x2="525" y2="290" stroke="#BECACD"/>
    <circle cx="315" cy="62"  r="25" fill="#fff" stroke="#60757A"/>
    <circle cx="175" cy="176" r="25" fill="#fff" stroke="#60757A"/>
    <circle cx="455" cy="176" r="25" fill="#fff" stroke="#60757A"/>
    <circle cx="105" cy="290" r="25" fill="#fff" stroke="#60757A"/>
    <circle cx="245" cy="290" r="25" fill="#fff" stroke="#60757A"/>
    <circle cx="385" cy="290" r="25" fill="#fff" stroke="#60757A"/>
    <circle cx="525" cy="290" r="25" fill="#fff" stroke="#60757A"/>
    <text x="315" y="22"  text-anchor="middle" fill="#60757A">v</text>
    <text x="105" y="296" text-anchor="middle" fill="#60757A">a</text>
    <text x="245" y="296" text-anchor="middle" fill="#60757A">b</text>
    <text x="385" y="296" text-anchor="middle" fill="#60757A">c</text>
    <text x="525" y="296" text-anchor="middle" fill="#60757A">d</text>
    <text x="105" y="332" text-anchor="middle" fill="#BECACD" style="font-size:13px">0</text>
    <text x="245" y="332" text-anchor="middle" fill="#BECACD" style="font-size:13px">1</text>
    <text x="385" y="332" text-anchor="middle" fill="#BECACD" style="font-size:13px">2</text>
    <text x="525" y="332" text-anchor="middle" fill="#60757A" style="font-size:13px">3</text>
    <!-- v2: one new root→leaf path; dashed arrows = pointers back into v.
         Both arrows clear every grey node by ~18 units at this scale. -->
    <text x="700" y="22" text-anchor="middle" fill="#017E5B">v2 = (assoc v 3 x)</text>
    <g v-click>
      <line x1="700" y1="62"  x2="740" y2="176" stroke="#017E5B"/>
      <line x1="740" y1="176" x2="780" y2="290" stroke="#017E5B"/>
      <line class="share-link" x1="700" y1="62"  x2="214" y2="167" stroke="#017E5B" marker-end="url(#share-arrow)"/>
      <line class="share-link" x1="740" y1="176" x2="423" y2="278" stroke="#017E5B" marker-end="url(#share-arrow)"/>
      <circle cx="700" cy="62"  r="25" fill="#D1F7D2" stroke="#017E5B"/>
      <circle cx="740" cy="176" r="25" fill="#D1F7D2" stroke="#017E5B"/>
      <circle cx="780" cy="290" r="25" fill="#D1F7D2" stroke="#017E5B"/>
      <text x="780" y="296" text-anchor="middle" fill="#017E5B">x</text>
      <text x="780" y="332" text-anchor="middle" fill="#017E5B" style="font-size:13px">3</text>
    </g>
  </g>
</svg>

<p v-after class="muted small">Three new nodes — everything else is shared.</p>

<!--
The slide the math/EE crowd enjoys most — immutability goes from virtue to engineering solution. The diagram carries it alone now, so these three facts are YOURS to say: (1) persistent vectors are wide trees — 32-way tries, drawn binary here to fit; (2) assoc copies ~log₃₂ n small nodes and shares the rest; (3) a 1M-element vector: a full copy moves 10⁶ refs, assoc copies ~10² — four orders of magnitude less work. Beat: v alone first ("we're about to assoc at slot 3"), one click drops in v2 — three green nodes, the dashed pointers reach back into everything reused. log₃₂(10⁹) ≈ 6: effectively constant. Orally: on the wall clock that's ≈40 ms vs ≈0.01 ms for 1M elements — you'll prove the timings live in session 1.
-->

---

<div class="eyebrow">VALUES</div>

## The industry agrees

<div style="margin-top:2em;">
<div class="power-label">PLACES BY DEFAULT · VALUES OPT-IN</div>
<div class="lang-rows">
  <span class="lang">Java</span><span class="muted">records · <code>List.of</code> · JEP 401: Value Objects</span>
  <span class="lang">C#</span><span class="muted">records · <code>readonly</code></span>
  <span class="lang">TypeScript</span><span class="muted"><code>as const</code> · <code>Readonly&lt;T&gt;</code> · Immer</span>
  <span class="lang">Kotlin</span><span class="muted"><code>kotlinx.collections.immutable</code> — persistent collections, inspired by Clojure’s</span>
</div>
</div>

<!--
Generous framing, no dunking. Say out loud: Java and C# are genuinely moving this way (records, value classes on the roadmap), yet the DEFAULT is still places — values are something you opt into. Kotlin is the sharpest receipt: JetBrains officially ships kotlinx.collections.immutable — persistent collections with structural sharing, inspired by Clojure's design (newer CHAMP-based tries under the hood) — and still opt-in; the default List is a place. Beat into the next slide: "all your languages are adding this…"
-->

---

<div class="eyebrow">VALUES</div>

## …and some just start there

<div style="margin-top:2.2em;">
<div class="power-label">VALUES BY DEFAULT</div>
<div class="lang-rows">
  <span class="lang green">Haskell</span><span class="muted">purity enforced by the type system</span>
  <span class="lang green">F#</span><span class="muted">immutable by default — mutation is the extra keyword</span>
  <span class="lang">Scala</span><span class="muted">the default collections are persistent — inspired by Clojure’s</span>
</div>
</div>

<div style="margin-top:1.8em;">
<p class="power"><span class="green">Clojure</span> — values all the way down</p>
<p class="muted">Every core data structure, the entire standard library, the ecosystem’s idioms.</p>
</div>

<!--
"…and these just start there." Haskell is the strictest — purity enforced by types, mutation quarantined in IO/ST. F# flips the default: let is immutable, mutation needs the extra keyword (though it sits on the mutable .NET base library). Scala is the honest middle: the default collection library IS persistent (Vector's 32-way trie modeled on Clojure's, CHAMP maps/sets since 2.13), but var is as frictionless as val — idiom carries it, not the language. Clojure starts at values for everything — every core data structure, the whole stdlib, the ecosystem's idioms. That's the difference between a feature and a paradigm.
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

<div class="cols homoicon">

<div class="form-anat">
  <span class="glyph paren">(</span>
  <span class="glyph el">+</span>
  <span class="glyph el">1</span>
  <span class="glyph el">2</span>
  <span class="glyph paren">)</span>
  <span class="tick"></span>
  <span class="tick">first</span>
  <span class="tick">second</span>
  <span class="tick">third</span>
  <span class="tick"></span>
</div>

<div class="verdict">
  <div>
    <div class="power-label">WHAT THE COMPILER GETS</div>
    <p class="verdict-word">A list</p>
  </div>
  <div>
    <div class="power-label">NO SEPARATE AST</div>
    <p class="verdict-word green">Homoiconicity</p>
  </div>
</div>

</div>

<span class="switch">→ to the REPL · session: macros</span>

<!--
SWITCH 2 → deck (this one slide, a breather after 25 min of demo), then SWITCH 3 → editor for workshop/macros.clj: first/eval, unless, build my->, macroexpand-1, (source ->). ~15–20 min.

Say it, don’t read it: this is not text that LOOKS like a list — it IS a list, first element +, then 1, then 2. Nothing parses it into some other tree afterwards: the syntax tree is the syntax. That property has a name, homoiconicity — and it is the whole reason macros are ordinary code. Let’s use it.
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

<div class="cols" style="margin-top:2.4em; gap:2.5rem;">
  <div>
    <div class="power-label">Concurrency</div>
    <p class="power">No locks</p>
    <div class="power-stack">Immutable values<br>one shared <code>atom</code><br><code>swap!</code> with a pure function</div>
  </div>
  <div>
    <div class="power-label">Testing</div>
    <p class="power">No mocks</p>
    <div class="power-stack">Pure functions over values<br>data in, data out<br>assert with <code>=</code></div>
  </div>
  <div>
    <div class="power-label">Debugging</div>
    <p class="power">Replay the bug</p>
    <div class="power-stack">Values print and compare<br>a failing input is <em>just data</em><br>paste it back into the REPL</div>
  </div>
</div>

<!--
Tie back to pains they have this week. If ahead of schedule: 60-second live atom demo — (def a (atom {})) (swap! a assoc :x 1) — point out swap! takes a pure function of value → value.
-->

---

<div class="eyebrow">IN PRACTICE</div>

## Libraries take <span class="green">data</span>, not syntax

<div class="cols code-sm eq-code" style="margin-top:1.8em; gap:2rem;">
<div>
<div class="power-label">Routes · reitit</div>

```clojure
[["/users"
  {:get list-users}]
 ["/users/:id"
  {:get show-user}]]
```

</div>
<div>
<div class="power-label">HTML · hiccup</div>

```clojure
[:div
 [:h2 "Users"]
 [:a {:href "/users/42"}
  "Ada Lovelace"]]
```

</div>
<div>
<div class="power-label">SQL · honeysql</div>

```clojure
{:select [:id :name]
 :from   [:users]
 :where  [:= :id 42]}
```

</div>
</div>


<!--
Echo of the thesis slide: language features are libraries, and libraries take data. Read the three columns as ONE story — a route table pointing at /users/:id, a page linking to /users/42, the query that fetches user 42. These are the ecosystem defaults, not fringe libraries. Verbal beats on the kicker: because it's data and not string concatenation, SQL injection and unescaped HTML are structurally off the table (honeysql emits parameterized queries, hiccup escapes by default); and "your routes are a value" means you can print them, diff them between deploys, test them with = — everything from the debugging column applies to your whole web stack. If asked how data becomes SQL/HTML: one function call — (sql/format ...), (html ...) — at the edge.
-->

---

<div class="eyebrow">ADOPTION · REPRISE</div>

## <span class="green">Hosted</span>, cashed out

<p class="muted">The hosts from the opening — this time read from the stack you already run.</p>

<div class="lang-rows" style="margin-top:1.8em; row-gap:0.7em;">
  <span class="lang ink">Java</span><span class="muted">call any JVM library, deploy a jar, keep your profilers and your ops</span>
  <span class="lang ink">JavaScript</span><span class="muted">ClojureScript compiles to JS — runs in the browser, React through Reagent</span>
  <span class="lang ink">C#</span><span class="muted">ClojureCLR runs on .NET — same language, full CLR interop</span>
  <span class="lang ink">C++</span><span class="muted">jank runs on LLVM — near-native performance, easy C bindings</span>
  <span class="lang ink">Shell</span><span class="muted">Babashka — Clojure scripts with instant startup</span>
</div>

<p v-click style="margin-top:2.2em;">Curiosity is low-risk: a library, a script, a build tool — <span class="green">not a rewrite</span>.</p>

<!--
The reprise — say the callback OUT LOUD: "you saw this triple at the top of the hour as a design principle; here it is as a risk assessment." Same facts, arrow reversed: the opening slide read Clojure → host, this one reads your stack → your entry point. Babashka earns its row for the shell-scripting crowd — Clojure with instant startup. jank is the newest host: Clojure on LLVM with seamless C/C++ interop — worth saying out loud that it's young (fresh out of alpha) while the other hosts are decades-proven. One beat: the kicker. The next two slides are the receipts: interop in both directions, in code.
-->

---

<div class="eyebrow">ADOPTION · INTEROP</div>

## Java from Clojure — <span class="green">no FFI</span>

```clojure
(import (java.time LocalDate))

(def date (LocalDate/parse "2026-08-10"))   ; static method
(.plusDays date 30)                         ; instance method → a new LocalDate
(java.io.File. "deps.edn")                  ; constructor — the trailing dot

(.toUpperCase "fagdag")                     ; => "FAGDAG" — strings ARE java.lang.String

;; host calls chain like any Clojure code
(-> date (.plusDays 4) .getDayOfWeek str)   ; => "FRIDAY"
```

<p style="margin-top:1.2em;">No wrappers, no bindings, no marshalling — <span class="green">the dot is the whole FFI</span>.</p>
<p class="muted">Every library on Maven Central is one <code>deps.edn</code> line away. Don’t port it — call it.</p>

<!--
Receipt #1 — the everyday direction. Say it: the dot IS the FFI. This is not a bindings layer; the compiler emits the same bytecode javac would (add a type hint and there's no reflection either). The .toUpperCase line is the "hosted" slide's claim made concrete — Clojure strings, numbers and collections ARE host objects, nothing crosses a boundary because there is no boundary. Bonus beats, verbal: Clojure fns implement Runnable and Callable, so you can hand one straight to an ExecutorService; and the threading macro from act two works on host calls too — interop code still reads like Clojure. If someone asks about the bare .getDayOfWeek in the thread: (-> x .foo) expands to (.foo x), parens are optional at arity 1.
-->

---

<div class="eyebrow">ADOPTION · INTEROP</div>

## Clojure from Java — <span class="green">just a static method</span>

<div class="cols code-sm">
<div>

```java
package acme;

record Item(double price, int qty) {}
record Order(List<Item> items) {}

var order = new Order(List.of(new Item(129, 2)));

double total = Pricing.quote(order);  // 258.0
```

</div>
<div>

```clojure
;; acme/pricing.clj — the whole new module
(ns acme.pricing
  (:gen-class
   :name acme.Pricing
   :methods [^:static [quote [acme.Order] double]]))

(defn -quote [order]         ; ← that Java record,
  (->> (.items order)        ;   read with the same
       (map #(* (.price %)   ;   dot as last slide
                (.qty %)))
       (reduce +)))
```

</div>
</div>

<p style="margin-top:1.2em;">One new module in Clojure — <span class="green">the rest of the service never notices</span>.</p>

<!--
Receipt #2 — the partial-adoption clincher. Read it left to right: the Java service builds an Order exactly as it always has and calls Pricing.quote(order) — a plain static method, typed signature, IDE completion, nothing to react to. On the right: the ENTIRE new module. :gen-class does the bridging: :name picks the class, ^:static [quote [acme.Order] double] the signature, and -quote is the implementation (the "-" prefix is gen-class's default binding convention). The namespace is AOT-compiled at build time (tools.build compile-clj, or lein :aot; javac runs first so acme.Order exists) — Java gets a real class file in the same jar. The object crosses as an ordinary JVM reference — no DTO, no serialization; .items/.price/.qty are the record accessors via the same dot as the previous slide, and map/reduce run directly on the java.util.List because Clojure's sequence functions accept any Iterable. Return trip: (reduce +) yields a Double, gen-class unboxes it to double — 129.0 × 2 = 258.0. If someone asks about skipping the AOT step: the official low-ceremony API is clojure.java.api.Clojure + IFn — Clojure.var("clojure.core", "require") to load the namespace, then Clojure.var("acme.pricing", "quote").invoke(order) — no compile step, but stringly-typed; gen-class is the version Java teammates accept without flinching. Land the pitch: pilot on a leaf module with a clear boundary — pricing rules, a report generator, a data transform — and the rest of the codebase never has to know.
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
