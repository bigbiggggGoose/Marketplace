import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/theme.css'

import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/store/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

const auth = useAuthStore()
auth.hydrate()

app.mount('#app')
