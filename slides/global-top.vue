<template>
  <!-- Footer lives in global-TOP: the global-bottom layer sits below the slide,
       and .slidev-layout paints an opaque background that would cover it. -->
  <!-- REN logo (paths from @ren-no/design-system RENLogo, colors via logo tokens) -->
  <svg class="deck-footer-logo" viewBox="0 0 129 32" aria-label="REN">
    <g style="fill: var(--logo-ren-color-symbol)">
      <path d="M32.536 0H0.5V7.2381H32.536V0Z" />
      <path d="M32.536 12.381H0.499985V19.6191H32.536V12.381Z" />
      <path d="M32.536 24.7619H0.499985V32H32.536V24.7619Z" />
    </g>
    <g style="fill: var(--logo-ren-color-text)">
      <path d="M53.6103 20.1905H48.312V31.8095H41.6892V0H53.4211C57.2055 0 60.0439 0.761905 62.1254 2.47619C64.2068 4.19048 65.1529 6.47619 65.1529 9.52381C65.1529 11.619 64.7745 13.5238 63.8284 15.0476C62.8823 16.5714 61.5577 17.7143 59.4762 18.4762L66.2883 31.6191V32H59.287L53.6103 20.1905ZM48.312 14.8571H53.6103C55.3133 14.8571 56.4486 14.4762 57.3948 13.5238C58.3409 12.7619 58.7193 11.4286 58.7193 10.0952C58.7193 8.57143 58.3409 7.42857 57.3948 6.47619C56.6379 5.52381 55.3133 5.14286 53.4211 5.14286H48.1228V14.8571H48.312Z" />
      <path d="M92.072 18.0952H79.7092V26.6667H94.3198V32H73.1531V0H94.1325V5.33333H79.5219V12.9524H92.072V18.0952Z" />
      <path d="M128.072 32H121.495L108.906 11.0476V32H102.329V0H108.906L121.495 21.1429V0H128.072V32Z" />
    </g>
  </svg>
  <!-- current/total slide number, like reveal's slideNumber: 'c/t' -->
  <div class="deck-slide-number">
    {{ $slidev.nav.currentPage }} / {{ $slidev.nav.total }}
  </div>
</template>
