import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './styles/main.css'
// @ts-ignore
import App from './App.vue'

// 导入 highlight.js 用于代码高亮
// @ts-ignore
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// 全局注册 hljs
(window as any).hljs = hljs

const app = createApp(App)
const pinia = createPinia()

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(ElementPlus)
app.mount('#app')