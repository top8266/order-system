<template>
  <div style="background:#fff; min-height:100vh;">
    <div style="background:#222; padding:14px; text-align:center;">
      <template v-if="currentUser">
        <span style="color:#67c23a; margin:0 20px;">👤 {{ currentUser.username }}</span>
        <span style="color:#f56c6c; margin:0 20px; text-decoration:none; cursor:pointer;" @click="logout">退出</span>
        <span style="color:#999; margin:0 10px;">|</span>
      </template>
      <template v-else>
        <router-link to="/auth" style="color:#409eff; margin:0 20px; text-decoration:none;">登录</router-link>
        <span style="color:#999; margin:0 10px;">|</span>
      </template>
      <router-link to="/buyer" style="color:white; margin:0 20px; text-decoration:none;">用户下单</router-link>
      <router-link to="/seller" style="color:white; margin:0 20px; text-decoration:none;">订单管理</router-link>
      <router-link to="/delivery" style="color:white; margin:0 20px; text-decoration:none;">接单配送</router-link>
      <router-link to="/admin" style="color:white; margin:0 20px; text-decoration:none;">系统管理</router-link>
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