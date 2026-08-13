<script setup>
// 13 cells so the visible window (9 cells) stays covered at every shift.
// Index 8 is the cell the head writes and later erases — rendered as two
// stacked glyphs whose opacities swap on the shared timeline.
const cells = ['1', '0', '1', '1', '0', '0', '1', '0', null, '1', '0', '1', '0']

// running=false parks the machine: animations drop away and every element
// snaps to its base rest pose (tape home, reels level, cell 8 showing 0).
defineProps({ running: { type: Boolean, default: true } })
</script>

<template>
  <g class="turing-tape" :class="{ 'is-static': !running }" aria-hidden="true">
    <defs>
      <clipPath id="tt-window">
        <rect x="4" y="65" width="252" height="34"/>
      </clipPath>
    </defs>
    <!-- reels: spokes are three full diameters so the group's fill-box
         center coincides with the reel center (rotation stays true) -->
    <g transform="translate(70, 36)">
      <circle r="25" fill="none" stroke="#A3CFFF" stroke-width="1.5"/>
      <g class="tt-spin">
        <line x1="-25" y1="0" x2="25" y2="0" stroke="#A3CFFF" stroke-width="1.5"/>
        <line x1="-12.5" y1="-21.65" x2="12.5" y2="21.65" stroke="#A3CFFF" stroke-width="1.5"/>
        <line x1="-12.5" y1="21.65" x2="12.5" y2="-21.65" stroke="#A3CFFF" stroke-width="1.5"/>
      </g>
      <circle r="3.5" fill="#FFFFFF" stroke="#19547D" stroke-width="1.5"/>
    </g>
    <g transform="translate(190, 36)">
      <circle r="25" fill="none" stroke="#A3CFFF" stroke-width="1.5"/>
      <g class="tt-spin">
        <line x1="-25" y1="0" x2="25" y2="0" stroke="#A3CFFF" stroke-width="1.5"/>
        <line x1="-12.5" y1="-21.65" x2="12.5" y2="21.65" stroke="#A3CFFF" stroke-width="1.5"/>
        <line x1="-12.5" y1="21.65" x2="12.5" y2="-21.65" stroke="#A3CFFF" stroke-width="1.5"/>
      </g>
      <circle r="3.5" fill="#FFFFFF" stroke="#19547D" stroke-width="1.5"/>
    </g>
    <!-- tape guides: tangent lines from the reel rims (wrapping over the
         outer top of each spool) down into the window's top corners -->
    <line x1="52.4" y1="18.2" x2="4" y2="66" stroke="#D1E8FF" stroke-width="1.5"/>
    <line x1="207.6" y1="18.2" x2="256" y2="66" stroke="#D1E8FF" stroke-width="1.5"/>
    <!-- read/write head, fixed over the window center -->
    <path d="M 120 51 H 140 L 130 64 Z" fill="#19547D"/>
    <!-- write flash behind the cell under the head -->
    <rect class="tt-flash" x="116" y="66" width="28" height="32" fill="#D1E8FF"/>
    <!-- the tape itself: steps sideways under the head -->
    <g clip-path="url(#tt-window)">
      <g class="tt-strip">
        <template v-for="(s, i) in cells" :key="i">
          <rect :x="-52 + i * 28" y="66" width="28" height="32" fill="none" stroke="#D1E8FF" stroke-width="1.5"/>
          <text v-if="s !== null" class="tt-sym" :x="-38 + i * 28" y="83">{{ s }}</text>
          <g v-else>
            <text class="tt-sym tt-w0" :x="-38 + i * 28" y="83">0</text>
            <text class="tt-sym tt-w1" :x="-38 + i * 28" y="83">1</text>
          </g>
        </template>
      </g>
    </g>
  </g>
</template>

<style scoped>
.tt-sym {
  font-family: 'IBM Plex Mono', ui-monospace, 'Cascadia Mono', Menlo, monospace;
  font-size: 15px;
  fill: #19547D;
  text-anchor: middle;
  dominant-baseline: central;
}

/* One 14s timeline shared by every animation, so tape steps, reel turns,
   flashes and symbol swaps stay in lockstep. The head's walk:
   read at home → step right twice → write 1 → walk back → read →
   step right twice → erase back to 0 → walk home (seamless loop). */
.tt-strip {
  animation: tt-shift 14s ease-in-out infinite;
}
.tt-spin {
  transform-box: fill-box;
  transform-origin: center;
  animation: tt-rotate 14s ease-in-out infinite;
}
.tt-flash {
  opacity: 0;
  animation: tt-flash 14s linear infinite;
}
.tt-w0 { animation: tt-write0 14s linear infinite; }
.tt-w1 { opacity: 0; animation: tt-write1 14s linear infinite; }

@keyframes tt-shift {
  0%, 8%    { transform: translateX(0); }
  12%, 18%  { transform: translateX(-28px); }
  22%, 34%  { transform: translateX(-56px); }
  38%, 44%  { transform: translateX(-28px); }
  48%, 58%  { transform: translateX(0); }
  62%, 66%  { transform: translateX(-28px); }
  70%, 82%  { transform: translateX(-56px); }
  86%, 90%  { transform: translateX(-28px); }
  94%, 100% { transform: translateX(0); }
}
@keyframes tt-rotate {
  0%, 8%    { transform: rotate(0deg); }
  12%, 18%  { transform: rotate(30deg); }
  22%, 34%  { transform: rotate(60deg); }
  38%, 44%  { transform: rotate(30deg); }
  48%, 58%  { transform: rotate(0deg); }
  62%, 66%  { transform: rotate(30deg); }
  70%, 82%  { transform: rotate(60deg); }
  86%, 90%  { transform: rotate(30deg); }
  94%, 100% { transform: rotate(0deg); }
}
@keyframes tt-write0 {
  0%, 25%   { opacity: 1; }
  27%, 73%  { opacity: 0; }
  75%, 100% { opacity: 1; }
}
@keyframes tt-write1 {
  0%, 25%   { opacity: 0; }
  27%, 73%  { opacity: 1; }
  75%, 100% { opacity: 0; }
}
@keyframes tt-flash {
  0%, 24%   { opacity: 0; }
  26%       { opacity: 0.9; }
  30%, 51%  { opacity: 0; }
  53%       { opacity: 0.4; }
  57%, 72%  { opacity: 0; }
  74%       { opacity: 0.9; }
  78%, 100% { opacity: 0; }
}

.turing-tape.is-static .tt-strip,
.turing-tape.is-static .tt-spin,
.turing-tape.is-static .tt-flash,
.turing-tape.is-static .tt-w0,
.turing-tape.is-static .tt-w1 {
  animation: none;
}

@media (prefers-reduced-motion: reduce) {
  .tt-strip, .tt-spin, .tt-flash, .tt-w0, .tt-w1 {
    animation: none;
  }
}
</style>
