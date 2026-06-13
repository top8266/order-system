<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="auth-header">
        <h1>{{ isLogin ? '登录' : '注册' }}</h1>
        <p class="auth-subtitle">{{ isLogin ? '欢迎回来' : '创建新账户' }}</p>
      </div>

      <div class="auth-tabs">
        <button :class="{ active: isLogin }" @click="isLogin = true">登录</button>
        <button :class="{ active: !isLogin }" @click="isLogin = false">注册</button>
      </div>

      <!-- 登录表单 -->
      <div v-if="isLogin" class="auth-form">
        <div class="form-item">
          <label>用户名</label>
          <input v-model="loginForm.username" placeholder="请输入用户名" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </div>
        <button class="auth-btn" @click="handleLogin">登录</button>
      </div>

      <!-- 注册表单 -->
      <div v-if="!isLogin" class="auth-form">
        <div class="form-item">
          <label>用户名</label>
          <input v-model="registerForm.username" placeholder="请输入用户名" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        </div>
        <div class="form-item">
          <label>角色</label>
          <select v-model="registerForm.role">
            <option value="buyer">普通用户</option>
            <option value="seller">商家</option>
            <option value="delivery">配送员</option>
          </select>
        </div>
        <div class="form-item">
          <label>电话</label>
          <input v-model="registerForm.phone" placeholder="请输入联系电话" />
        </div>
        <div v-if="registerForm.role === 'seller'" class="form-item">
          <label>店铺名称</label>
          <input v-model="registerForm.shopName" placeholder="请输入店铺名称" />
        </div>
        <div v-if="registerForm.role === 'delivery'" class="form-item">
          <label>真实姓名</label>
          <input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </div>
        <button class="auth-btn" @click="handleRegister">注册</button>
      </div>

      <div class="auth-footer">
        <router-link to="/buyer" class="back-link">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return {
      isLogin: true,
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        password: '',
        role: 'buyer',
        phone: '',
        shopName: '',
        realName: ''
      }
    }
  },
  methods: {
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        alert('请填写完整信息')
        return
      }
      try {
        const { data } = await axios.post('http://localhost:8080/auth/login', this.loginForm)
        if (data.success) {
          // 保存token和用户信息
          localStorage.setItem('token', data.token)
          localStorage.setItem('user', JSON.stringify(data.user))
          alert(data.message)
          // 根据角色跳转
          this.redirectByRole(data.user.role)
        } else {
          alert(data.message)
        }
      } catch (error) {
        alert('登录失败，请检查网络')
      }
    },
    async handleRegister() {
      if (!this.registerForm.username || !this.registerForm.password) {
        alert('请填写完整信息')
        return
      }
      try {
        const { data } = await axios.post('http://localhost:8080/auth/register', this.registerForm)
        if (data.success) {
          alert(data.message)
          this.isLogin = true
          this.loginForm.username = this.registerForm.username
        } else {
          alert(data.message)
        }
      } catch (error) {
        alert('注册失败，请检查网络')
      }
    },
    redirectByRole(role) {
      switch (role) {
        case 'buyer':
          this.$router.push('/buyer')
          break
        case 'seller':
          this.$router.push('/seller')
          break
        case 'delivery':
          this.$router.push('/delivery')
          break
        case 'admin':
          this.$router.push('/admin')
          break
        default:
          this.$router.push('/buyer')
      }
    }
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.auth-box {
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.auth-header h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
}

.auth-subtitle {
  color: #666;
  font-size: 14px;
}

.auth-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
}

.auth-tabs button {
  flex: 1;
  padding: 12px;
  border: none;
  background: #f5f7fa;
  color: #666;
  border-radius: 10px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.auth-tabs button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.auth-form {
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-item input,
.form-item select {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e5e6eb;
  border-radius: 10px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-item input:focus,
.form-item select:focus {
  outline: none;
  border-color: #667eea;
}

.auth-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.auth-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.auth-footer {
  text-align: center;
  margin-top: 20px;
}

.back-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}
</style>