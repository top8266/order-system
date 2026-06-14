<template>
  <div style="background:#fff; min-height:100vh;">
    <div style="background:#222; padding:14px; display:flex; justify-content:space-between; align-items:center;">
      <div style="display:flex; gap:20px;">
        <router-link to="/buyer" style="color:white; text-decoration:none;">用户下单</router-link>
        <router-link to="/seller" style="color:white; text-decoration:none;">订单管理</router-link>
        <router-link to="/delivery" style="color:white; text-decoration:none;">接单配送</router-link>
        <router-link to="/admin" style="color:white; text-decoration:none;">系统管理</router-link>
      </div>
      <div style="display:flex; align-items:center; gap:15px;">
        <template v-if="currentUser">
          <span style="color:#67c23a;">👤 {{ currentUser.username }}</span>
          <span style="color:#999;">|</span>
          <span style="color:#f56c6c; cursor:pointer; text-decoration:underline;" @click="logout">退出登录</span>
        </template>
        <template v-else>
          <router-link to="/auth" style="color:#409eff; text-decoration:none;">🔐 登录</router-link>
        </template>
      </div>
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
      this.$router.push('/auth')
    }
  },
  watch: {
    $route() {
      this.checkLogin()
    }
  }
}
</script>