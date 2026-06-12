import { createRouter, createWebHashHistory } from 'vue-router'
import Buyer from '../views/Buyer.vue'
import Seller from '../views/Seller.vue'
import Delivery from '../views/Delivery.vue'

const routes = [
  { path: '/', redirect: '/buyer' },
  { path: '/buyer', name: 'Buyer', component: Buyer },
  { path: '/seller', name: 'Seller', component: Seller },
  { path: '/delivery', name: 'Delivery', component: Delivery }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router