import { createRouter, createWebHashHistory } from 'vue-router'
import Buyer from '../views/Buyer.vue'
import Seller from '../views/Seller.vue'
import Delivery from '../views/Delivery.vue'
import Admin from '../views/Admin.vue'
import Auth from '../views/Auth.vue'
import AddressManager from '../views/AddressManager.vue'

// 路由守卫：检查用户是否已登录
const requireAuth = (to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  
  if (!token || !userStr) {
    next('/auth')
    return
  }
  
  try {
    const user = JSON.parse(userStr)
    // 检查角色权限
    const role = to.meta.role
    if (role && user.role !== role) {
      // 角色不匹配，跳转到对应角色的页面
      const roleRoutes = {
        'buyer': '/buyer',
        'seller': '/seller',
        'delivery': '/delivery',
        'admin': '/admin'
      }
      next(roleRoutes[user.role] || '/buyer')
      return
    }
    next()
  } catch (e) {
    next('/auth')
  }
}

const routes = [
  { path: '/', redirect: '/auth' },
  { path: '/auth', name: 'Auth', component: Auth },
  { path: '/buyer', name: 'Buyer', component: Buyer, meta: { role: 'buyer' } },
  { path: '/seller', name: 'Seller', component: Seller, meta: { role: 'seller' } },
  { path: '/delivery', name: 'Delivery', component: Delivery, meta: { role: 'delivery' } },
  { path: '/admin', name: 'Admin', component: Admin, meta: { role: 'admin' } },
  { path: '/address', name: 'AddressManager', component: AddressManager }
]

// 在挂载路由之前先创建路由实例
const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 添加全局前置守卫
router.beforeEach((to, from, next) => {
  // 如果是访问 auth 页面，直接放行
  if (to.path === '/auth') {
    next()
    return
  }
  
  // 如果访问需要权限的页面
  if (to.meta.role) {
    requireAuth(to, from, next)
  } else {
    next()
  }
})

export default router