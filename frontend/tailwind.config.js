/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Gece müzayedesi — sıcak, koyu zemin
        ink: {
          950: '#0E0C0A',
          900: '#16130F',
          800: '#201B15',
          700: '#2B2419',
          600: '#3A3122',
          500: '#544733',
        },
        // Açık artırma paylaşma kağıdı / bilet zemini
        paper: {
          DEFAULT: '#F2EADA',
          dim: '#E4D8BE',
          line: '#D2C3A0',
        },
        // Pirinç paddle / vurgu rengi
        gold: {
          300: '#EAD08B',
          400: '#DDB35C',
          500: '#C89A3D',
          600: '#A87A26',
          700: '#7F5C1B',
        },
        // Canlı / son saniye vurgusu
        ember: {
          400: '#E06A4C',
          500: '#CC4A30',
          600: '#A83722',
        },
      },
      fontFamily: {
        display: ['"Fraunces"', 'serif'],
        sans: ['"Public Sans"', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
      backgroundImage: {
        grain: "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.4'/%3E%3C/svg%3E\")",
      },
    },
  },
  plugins: [],
}
