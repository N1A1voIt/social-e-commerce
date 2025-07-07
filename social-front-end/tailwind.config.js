module.exports = {
  content: [
    "./src/**/*.{html,ts}"  // scans components and templates
  ],
  theme: {
    extend: {
      fontFamily: {
        'custom-font': ['MonaSans', 'sans-serif'], // 'custom-font' is your Tailwind utility, 'MyCustomFont' matches your @font-face name
      }
    },
    colors: {
      blackt: '#252C35',
      gray: '#666667',
      principal : '#F1EAEA',
      white :'#E9E8E9',
      black:'#212429',
      brown:'#D4C4B8',
      yellow:'#E8BC34',
      orange:'#FC6657',
      cardbg:'#EBDDDB'
    }
  },
  plugins: [],
};
