<template>
  <div class="container">
    <header class="page-header">
      <h1 class="page-title">🔧 系统管理后台</h1>
    </header>

    <!-- 数据统计卡片 -->
    <div class="stat-wrap">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-num">{{ stats.totalOrders }}</div>
          <div class="stat-label">全部订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <div class="stat-num">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👤</div>
        <div class="stat-content">
          <div class="stat-num">{{ stats.totalSellers }}</div>
          <div class="stat-label">商家数量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <div class="stat-num">{{ stats.totalRevenue.toFixed(2) }}</div>
          <div class="stat-label">总销售额</div>
        </div>
      </div>
    </div>

    <!-- 商家管理区域 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">👤 商家账户管理</h2>
        <button class="add-btn" @click="showAddSellerDialog=true">+ 新增商家</button>
      </div>

      <div class="table-container">
        <div class="table-scroll">
          <table class="data-table">
            <thead>
              <tr>
                <th width="60">ID</th>
                <th>用户名</th>
                <th>角色</th>
                <th>状态</th>
                <th width="180">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in sellers" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>
                  <span class="tag tag-role">{{ user.role }}</span>
                </td>
                <td>
                  <span class="tag" :class="user.status === 1 ? 'tag-active' : 'tag-disabled'">
                    {{ user.status === 1 ? '正常' : '已禁用' }}
                  </span>
                </td>
                <td>
                  <button class="btn-sm warning" @click="toggleUserStatus(user)">
                    {{ user.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button class="btn-sm danger" @click="deleteUser(user.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- 系统日志区域 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">📋 系统操作日志</h2>
        <button class="refresh-btn" @click="refreshAll">刷新数据</button>
      </div>

      <div class="table-container">
        <div class="table-scroll">
          <table class="data-table">
            <thead>
              <tr>
                <th width="60">ID</th>
                <th>操作类型</th>
                <th>操作内容</th>
                <th>操作时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs" :key="log.id">
                <td>{{ log.id }}</td>
                <td>
                  <span class="tag" :class="getLogClass(log.type)">{{ log.type }}</span>
                </td>
                <td>{{ log.content }}</td>
                <td>{{ log.time }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- 新增商家弹窗 -->
    <div class="dialog-mask" v-if="showAddSellerDialog" @click="showAddSellerDialog=false"></div>
    <div class="dialog" v-if="showAddSellerDialog">
      <div class="dialog-head">
        <h3>新增商家账户</h3>
        <button @click="showAddSellerDialog=false" class="close">×</button>
      </div>
      <div class="dialog-body">
        <div class="form-item">
          <label>用户名</label>
          <input v-model="newSeller.username" placeholder="请输入用户名" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="newSeller.password" type="password" placeholder="请输入密码" />
        </div>
        <div class="form-item">
          <label>角色</label>
          <select v-model="newSeller.role">
            <option value="seller">商家</option>
            <option value="delivery">配送员</option>
          </select>
        </div>
      </div>
      <div class="dialog-footer">
        <button @click="showAddSellerDialog=false" class="btn default">取消</button>
        <button @click="addSeller" class="btn primary">确认添加</button>
      </div>
    </div>

  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return {
      stats: {
        totalOrders: 0,
        totalProducts: 0,
        totalSellers: 0,
        totalRevenue: 0
      },
      sellers: [],
      logs: [],
      
      showAddSellerDialog: false,
      newSeller: {
        username: '',
        password: '',
        role: 'seller'
      }
    }
  },
  methods: {
    getLogClass(type) {
      switch(type) {
        case '新增': return 'tag-success'
        case '修改': return 'tag-warning'
        case '删除': return 'tag-danger'
        case '登录': return 'tag-info'
        default: return 'tag-default'
      }
    },
    async refreshAll() {
      await this.getStats()
      await this.getSellers()
      await this.getLogs()
    },
    async getStats() {
      const {data: orders} = await axios.get('http://localhost:8080/order/list')
      const {data: products} = await axios.get('http://localhost:8080/order/product/list')
      const {data: sellers} = await axios.get('http://localhost:8080/admin/sellers')
      
      this.products = products
      this.stats.totalOrders = orders.length
      this.stats.totalProducts = products.length
      this.stats.totalSellers = sellers.length
      
      // 计算总销售额（已完成订单）
      let revenue = 0
      for (let order of orders) {
        if (order.status === '已送达' || order.status === '已完成') {
          const product = products.find(p => p.id === order.productId)
          if (product) {
            revenue += product.price
          }
        }
      }
      this.stats.totalRevenue = revenue
    },
    async getSellers() {
      const {data} = await axios.get('http://localhost:8080/admin/sellers')
      this.sellers = data
    },
    async getLogs() {
      const {data} = await axios.get('http://localhost:8080/admin/logs')
      this.logs = data
    },
    async addSeller() {
      if (!this.newSeller.username || !this.newSeller.password) {
        alert('请填写完整信息')
        return
      }
      await axios.post('http://localhost:8080/admin/seller/add', this.newSeller)
      alert('商家添加成功')
      this.showAddSellerDialog = false
      this.newSeller = { username: '', password: '', role: 'seller' }
      this.refreshAll()
    },
    async toggleUserStatus(user) {
      const newStatus = user.status === 1 ? 0 : 1
      await axios.post('http://localhost:8080/admin/seller/status', {
        id: user.id,
        status: newStatus
      })
      alert(newStatus === 1 ? '已启用' : '已禁用')
      this.refreshAll()
    },
    async deleteUser(id) {
      if (confirm('确定要删除该用户？')) {
        await axios.post('http://localhost:8080/admin/seller/delete/' + id)
        alert('删除成功')
        this.refreshAll()
      }
    }
  },
  mounted() {
    this.refreshAll()
  }
}
</script>

<style scoped>
/* 全局容器 */
.container{
  min-height: 100vh;
  background: #ffffff;
  padding: 40px 20px;
}

/* 页面头部 */
.page-header{
  text-align: center;
  margin-bottom: 40px;
  color: #333;
}
.page-title{
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 10px;
  color: #000;
}

/* 统计卡片 */
.stat-wrap{
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}
.stat-card{
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 8px 25px rgba(0,0,0,0.08);
  transition: all 0.3s;
}
.stat-card:hover{
  transform: translateY(-5px);
  box-shadow: 0 12px 35px rgba(0,0,0,0.12);
}
.stat-icon{
  font-size: 40px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 15px;
}
.stat-content{
  flex: 1;
}
.stat-num{
  font-size: 36px;
  font-weight: 700;
  color: #2a3342;
}
.stat-label{
  font-size: 14px;
  color: #333;
  margin-top: 4px;
}

/* 区块 */
.section{
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
}
.section-header{
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}
.section-title{
  font-size: 22px;
  font-weight: 600;
  color: #2a3342;
  margin: 0;
}

/* 按钮 */
.add-btn{
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  color: #fff;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
}
.add-btn:hover{
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
}
.refresh-btn{
  background: #f5f7fa;
  color: #333;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}
.refresh-btn:hover{
  background: #e8eaed;
}

/* 表格容器 */
.table-container{
  background: #fafafa;
  border-radius: 16px;
  padding: 24px;
}
.table-scroll{
  max-height: 400px;
  overflow-y: scroll;
  border-radius: 12px;
  border: 2px solid #e5e6eb;
}
.table-scroll::-webkit-scrollbar{
  width: 10px;
}
.table-scroll::-webkit-scrollbar-track{
  background: #f1f1f1;
  border-radius: 5px;
}
.table-scroll::-webkit-scrollbar-thumb{
  background: #909399;
  border-radius: 5px;
}
.table-scroll::-webkit-scrollbar-thumb:hover{
  background: #606266;
}

/* 数据表格 */
.data-table{
  width: 100%;
  border-collapse: collapse;
}
.data-table th, .data-table td{
  padding: 16px 12px;
  text-align: left;
  border-bottom: 1px solid #e5e6eb;
}
.data-table th{
  background: #f5f7fa;
  font-weight: 600;
  color: #333;
  position: sticky;
  top: 0;
  z-index: 1;
}
.data-table tr:hover{
  background: #fafafa;
}

/* 状态标签 */
.tag{
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}
.tag-role{
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #1890ff;
}
.tag-active{
  background: linear-gradient(135deg, #f0f9eb 0%, #d4edda 100%);
  color: #67c23a;
}
.tag-disabled{
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}
.tag-success{
  background: linear-gradient(135deg, #f0f9eb 0%, #d4edda 100%);
  color: #67c23a;
}
.tag-warning{
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}
.tag-danger{
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}
.tag-info{
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #1890ff;
}
.tag-default{
  background: #f5f5f5;
  color: #666;
}

/* 小按钮 */
.btn-sm{
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  margin: 0 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
}
.btn-sm.success{
  background: #67c23a;
  color: #fff;
}
.btn-sm.success:hover{
  background: #85ce61;
}
.btn-sm.warning{
  background: #e6a23c;
  color: #fff;
}
.btn-sm.warning:hover{
  background: #ebb563;
}
.btn-sm.danger{
  background: #f56c6c;
  color: #fff;
}
.btn-sm.danger:hover{
  background: #f87171;
}
.btn-sm.primary{
  background: #409eff;
  color: #fff;
}
.btn-sm.primary:hover{
  background: #66b1ff;
}

/* 分类列表 */
.category-list{
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.category-item{
  background: #fff;
  padding: 12px 16px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #e5e6eb;
}
.category-name{
  font-weight: 500;
  color: #333;
}
.category-count{
  color: #666;
  font-size: 13px;
}

/* 弹窗遮罩 */
.dialog-mask{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  z-index: 1000;
  backdrop-filter: blur(4px);
}

/* 弹窗 */
.dialog{
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%,-50%);
  width: 450px;
  background: #fff;
  border-radius: 20px;
  z-index: 1001;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  animation: dialogScale 0.3s ease;
}
@keyframes dialogScale {
  from {
    opacity: 0;
    transform: translate(-50%,-50%) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translate(-50%,-50%) scale(1);
  }
}
.dialog-head{
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e6eb;
  font-size: 18px;
  font-weight: 600;
  color: #2a3342;
}
.close{
  border: none;
  background: none;
  font-size: 28px;
  cursor: pointer;
  color: #666;
  transition: color 0.3s;
}
.close:hover{
  color: #333;
}
.dialog-body{
  padding: 24px;
}
.form-item{
  margin-bottom: 20px;
}
.form-item label{
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}
.form-item input, .form-item select{
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e5e6eb;
  border-radius: 10px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}
.form-item input:focus, .form-item select:focus{
  outline: none;
  border-color: #409eff;
}
.dialog-footer{
  padding: 20px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e5e6eb;
}
.btn{
  padding: 12px 24px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}
.btn.primary{
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
}
.btn.primary:hover{
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
}
.btn.default{
  background: #f5f7fa;
  color: #333;
}
.btn.default:hover{
  background: #e8eaed;
}

/* 响应式 */
@media (max-width: 768px) {
  .container{
    padding: 20px 15px;
  }
  .page-title{
    font-size: 28px;
  }
  .stat-wrap{
    grid-template-columns: repeat(2, 1fr);
  }
  .dialog{
    width: 90%;
    max-width: 400px;
  }
  .data-table{
    font-size: 13px;
  }
  .data-table th, .data-table td{
    padding: 12px 8px;
  }
}
</style>