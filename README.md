# The other functional programming — Clojure workshop

A 90-minute talk for developers coming from Java, F#, and TypeScript.
Two big ideas: **values** and **code as data**. One deck, three demo files.

## Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure) (any recent version)
- Node.js — for the slides (Slidev) and the 30-second JavaScript hook

## Running things

**Slides** — a [Slidev](https://sli.dev) deck in `slides/`:

```sh
cd slides
npm install    # first time only
npm run dev    # serves and opens http://localhost:3030
```

Keys: `f` fullscreen, `o` overview, arrows to navigate. All delivery cues
live in the speaker notes — open <http://localhost:3030/presenter> for the
presenter view. The display/code webfonts are fetched online (falls back
to Georgia + system mono). `npm run export` renders a PDF.

The original reveal.js deck is parked in `slides/legacy-reveal/` (open
`index.html` in a browser) — delete it once the Slidev port has proven
itself in rehearsal.

**REPL** — from the project root:

```sh
clj                # plain REPL, or:
clj -M:nrepl       # headless nREPL on :7888 — connect from Zed / CIDER / Calva
```

Then load a session file and evaluate the `(comment ...)` forms one by one.
Evaluate, don't type — typing live under pressure eats minutes and momentum.

**The hook** — paste `examples/console_lies.js` into a *browser* DevTools
console (not Node — the lie is DevTools lazy expansion), then expand the
logged object after the mutation line runs.

## File map

| File | What it is | When |
|---|---|---|
| `slides/slides.md` | 18 slides + 2 backup appendix slides (Slidev) | blocks 1–3 |
| `examples/console_lies.js` | the console.log-that-lies demo behind slide 2 | 0:00 |
| `src/workshop/values.clj` | identity vs equality, non-destructive updates, structural-sharing timings | 0:25 |
| `src/workshop/data_oriented.clj` | tickets dataset, core vocabulary, `workload` report, nested updates | 0:30 |
| `src/workshop/macros.clj` | code-is-data, `unless`, `try-with` vs `with-open`, building `my->`, `macroexpand-1`, `(source ->)`, `my-some->` | 0:50 |

Note: `(source ->)` and `(source with-open)` need a real JVM Clojure REPL
(they read core sources from the classpath) — they won't work in babashka.

## Timeline

| Time | Segment | Where |
|---|---|---|
| 0:00–0:05 | Hook: the audit log that lies | slides 1–2 |
| 0:05–0:25 | Two traditions → PLOP → values → structural sharing | slides 3–11 |
| 0:25–0:50 | Live: values + data-oriented programming | `values.clj`, `data_oriented.clj` |
| 0:50 | One-slide breather: code is data | slide 12 |
| 0:50–1:10 | Live: `unless`, `try-with`, `my->`, `macroexpand`, `(source ->)` | `macros.clj` |
| 1:10–1:20 | Features-as-libraries thesis → practice → adoption → resources | slides 13–16 |
| 1:20–1:30 | Q&A buffer — don't plan content here | slide 17 |

## Rehearsal notes

- Switch between deck and editor only at section boundaries — four switches total.
- Huge font in the editor, light theme, close every panel you don't need.
- If the live macro build stumbles: appendix slide A has all `macroexpand`
  outputs precomputed. Show, narrate, move on.
- The demo files carry no expected-result comments — you evaluate them live.
  The comments that remain are delivery cues: what to say, not what prints.
- Macro session runs long. `try-with` — the beat aimed squarely at the Java
  half of the room — now runs early, so it's safe. Cut from the tail instead:
  `my-some->` (section 6, already flagged bonus), then `with-timing` (the
  generalization beat inside section 3).
