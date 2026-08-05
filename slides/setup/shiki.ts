import { defineShikiSetup } from '@slidev/types'

// The deck is light-only with dark code panels. Everforest's muted
// forest palette sits well on the REN deep-pine panel background.
export default defineShikiSetup(() => {
  return {
    themes: {
      dark: 'everforest-dark',
      light: 'everforest-dark',
    },
  }
})
