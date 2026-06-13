<template>
  <div style="background:#fff; min-height:100vh;">
    <div style="background:#222; padding:14px; text-align:center;">
      <router-link to="/buyer" style="color:white; margin:0 20px; text-decoration:none;">用户下单</router-link>
      <router-link to="/seller" style="color:white; margin:0 20px; text-decoration:none;">订单管理</router-link>
      <router-link to="/delivery" style="color:white; margin:0 20px; text-decoration:none;">接单配送</router-link>
      <router-link to="/admin" style="color:white; margin:0 20px; text-decoration:none;">系统管理</router-link>
      <span v-if="currentUser" style="color:#67c23a; margin-left:20px;">👤 {{ currentUser.username }}</span>
      <router-link v-if="currentUser" to="/auth" style="color:#f56c6c; margin-left:20px; text-decoration:none;" @click="logout">退出</router-link>
      <router-link v-else to="/auth" style="color:#409eff; margin-left:20px; text-decoration:none;">登录/注册</router-link>
    </div>
    <router-view />
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentUser: null
    }
  },
  mounted() {
    this.checkLogin()
  },
  methods: {
    checkLogin() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        this.currentUser = JSON.parse(userStr)
      }
    },
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      this.currentUser = null
    }
  },
  watch: {
    $route() {
      this.checkLogin()
    }
  }
}
</script>