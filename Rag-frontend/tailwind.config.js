/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#409EFF',
        secondary: '#67C23A',
        warning: '#E6A23C',
        danger: '#F56C6C',
        info: '#909399',
        light: '#F5F7FA',
        dark: '#1F2329',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        'soft': '0 2px 12px 0 rgba(0, 0, 0, 0.05)',
        'card': '0 4px 16px 0 rgba(0, 0, 0, 0.08)',
      },
    },
  },
  plugins: [],
}