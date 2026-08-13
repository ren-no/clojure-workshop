<script setup>
// running=false holds the resting form: animations drop away and the base
// styles show the static `factorial 3` under its definition line. Flipping
// to true starts the unfold from the top of its cycle.
defineProps({ running: { type: Boolean, default: true } })
</script>

<template>
  <g class="lambda-factorial" :class="{ 'is-static': !running }" aria-hidden="true">
    <!-- factorial 3 → 3 · factorial 2 → 3 · 2 · factorial 1 → 3 · 2 · 1 → 6.
         The word marches right, depositing each argument into a growing
         product row; at the base case it dissolves and the row condenses
         into a value. Static definition above explains each rewrite.
         Values are solid green; syntax is lighter machinery. -->
    <text class="lf-def" x="125.4" y="14">factorial = λn. n · factorial (n−1)</text>
    <g class="lf-expr">
      <text class="lf-syn lf-word" x="0" y="52">factorial</text>
      <text class="lf-syn lf-dot1" x="33" y="52">·</text>
      <text class="lf-syn lf-dot2" x="85.8" y="52">·</text>
      <text class="lf-val lf-d3" x="138.6" y="52">3</text>
      <text class="lf-val lf-d2" x="191.4" y="52">2</text>
      <text class="lf-val lf-d1" x="244.2" y="52">1</text>
      <text class="lf-val lf-result" x="59.4" y="52">6</text>
    </g>
  </g>
</template>

<style scoped>
.lambda-factorial text {
  font-family: 'IBM Plex Mono', ui-monospace, 'Cascadia Mono', Menlo, monospace;
  text-anchor: middle;
}
.lf-def { font-size: 14px; fill: #01C260; }
.lf-expr text { font-size: 22px; }
.lf-syn { fill: #01C260; }
.lf-val { fill: #017E5B; font-weight: 600; }
/* Must out-specify the `.lambda-factorial text` anchor rule above */
.lambda-factorial text.lf-word { text-anchor: start; }

/* One 16s timeline (offset from the tape machine's 14s so the doodles
   drift out of phase). Every digit slides exactly -132px into its product
   slot because the word's width plus gaps is constant. Position resets
   happen around 92%, while the element is hidden. */
.lf-word, .lf-d3, .lf-d2, .lf-d1 {
  animation-duration: 16s;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
}
.lf-word { animation-name: lf-word-move, lf-word-fade; }
.lf-d3 { animation-name: lf-d3-move, lf-d3-fade; }
.lf-d2 { opacity: 0; animation-name: lf-d2-move, lf-d2-fade; }
.lf-d1 { opacity: 0; animation-name: lf-d1-move, lf-d1-fade; }
.lf-dot1 { opacity: 0; animation: lf-dot1-fade 16s linear infinite; }
.lf-dot2 { opacity: 0; animation: lf-dot2-fade 16s linear infinite; }
.lf-result { opacity: 0; animation: lf-result-fade 16s linear infinite; }

/* factorial 3 → (14%) 3 · factorial 2 → (32%) 3 · 2 · factorial 1 →
   (50%) 3 · 2 · 1 → (64%) 6 → (88%) reset */
@keyframes lf-word-move {
  0%, 14%   { transform: translateX(0); }
  19%, 32%  { transform: translateX(52.8px); }
  37%, 90%  { transform: translateX(105.6px); }
  92%, 100% { transform: translateX(0); }
}
@keyframes lf-word-fade {
  0%, 50%   { opacity: 1; }
  55%, 92%  { opacity: 0; }
  96%, 100% { opacity: 1; }
}
@keyframes lf-d3-move {
  0%, 14%   { transform: translateX(0); }
  19%, 64%  { transform: translateX(-132px); }
  70%, 90%  { transform: translateX(-79.2px); }
  92%, 100% { transform: translateX(0); }
}
@keyframes lf-d3-fade {
  0%, 66%   { opacity: 1; }
  71%, 92%  { opacity: 0; }
  96%, 100% { opacity: 1; }
}
@keyframes lf-d2-move {
  0%, 32%   { transform: translateX(0); }
  37%, 88%  { transform: translateX(-132px); }
  92%, 100% { transform: translateX(0); }
}
@keyframes lf-d2-fade {
  0%, 17%   { opacity: 0; }
  22%, 66%  { opacity: 1; }
  71%, 100% { opacity: 0; }
}
@keyframes lf-d1-move {
  0%, 50%   { transform: translateX(0); }
  55%, 64%  { transform: translateX(-132px); }
  70%, 88%  { transform: translateX(-184.8px); }
  92%, 100% { transform: translateX(0); }
}
@keyframes lf-d1-fade {
  0%, 35%   { opacity: 0; }
  40%, 66%  { opacity: 1; }
  71%, 100% { opacity: 0; }
}
@keyframes lf-dot1-fade {
  0%, 15%   { opacity: 0; }
  20%, 64%  { opacity: 1; }
  69%, 100% { opacity: 0; }
}
@keyframes lf-dot2-fade {
  0%, 33%   { opacity: 0; }
  38%, 64%  { opacity: 1; }
  69%, 100% { opacity: 0; }
}
@keyframes lf-result-fade {
  0%, 66%   { opacity: 0; }
  72%, 88%  { opacity: 1; }
  94%, 100% { opacity: 0; }
}

.lambda-factorial.is-static text {
  animation: none;
}

@media (prefers-reduced-motion: reduce) {
  .lambda-factorial text {
    animation: none;
  }
}
</style>
