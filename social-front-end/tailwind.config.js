module.exports = {
  content: [
    "./src/**/*.{html,ts}"  // scans components and templates
  ],
  theme: {
    extend: {
      fontFamily: {
        'mono': ['Inter', 'sans-serif'], // 'custom-font' is your Tailwind utility, 'MyCustomFont' matches your @font-face name
      },
      fontSize: {
        // base mobile font sizes
        xl: ['2vh', { lineHeight: '1.75rem' }],    // 20px
        '2xl': ['3vh', { lineHeight: '2rem' }],     // 24px
        '3xl': ['4vh', { lineHeight: '2.25rem' }] // 30px
      },colors: {
        blackt:   '#1C1F26',   // Darker, cooler black for strong backgrounds
        fotsy:    '#FFFFFF',   // True white for high contrast
        gray:     '#7A7A8C',   // Neutral gray with better tone
        principal:'#EDECEA',   // Soft light gray, clean UI background
        white:    '#ffffff',   // Subtle off-white for cards or sections
        black:    '#1A1A1A',   // Strong black for headings/text
        brown:    '#B89C86',   // Soft earthy brown (less dusty)
        yellow:   '#C93355',   // Warmer, vibrant yellow
        orange:   '#FF6B4D',   // Slightly more vibrant for CTA buttons
        cardbg:   '#ECE7E5',   // Light warm gray for card backgrounds

      }

    },

  },
  plugins: [require('tailwind-scrollbar-hide')],
};
