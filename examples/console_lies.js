// The hook (slide 2). Paste into a BROWSER DevTools console — not Node.
// (Node's console.log serializes at log time, so the lie doesn't reproduce
// there. Browser consoles expand objects lazily; Chrome even admits it —
// hover the ⓘ icon: "Value below was evaluated just now".)

const config = { retries: 3, timeout: 3000 };

console.log(config);       // log the "before" state

// ...somewhere far away, months later:
config.timeout = 60000;    // "just a tweak"

// Now expand the logged object in the console:
//   ▸ { retries: 3, timeout: 60000 }
// The log shows the FUTURE. Even your debugger can't tell you what your
// data WAS — because it isn't a value, it's a place.
//
// Ask the room: how do we defend against this?
//   - console.log(structuredClone(config)) everywhere?
//   - Object.freeze?   (shallow, and throws only in strict mode)
//   - readonly types?  (erased at runtime — the aliasing is still real)
//   - discipline?
//
// What if this bug category simply couldn't exist?
