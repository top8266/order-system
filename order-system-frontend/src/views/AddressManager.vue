<template>
  <div class="address-page">
    <div class="address-panel">
    <div class="address-header">
      <div class="header-left">
        <button class="back-btn" @click="goBack">← 返回</button>
        <h3>📍 收货地址管理</h3>
      </div>
      <button class="add-btn" @click="showAddDialog=true">+ 新增地址</button>
    </div>

    <div class="address-list">
      <div v-for="addr in addresses" :key="addr.id" class="address-item" :class="{ default: addr.isDefault === 1 }">
        <div class="address-info">
          <div class="address-name">
            <span>{{ addr.name }}</span>
            <span class="address-phone">{{ addr.phone }}</span>
            <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
          </div>
          <div class="address-detail">
            {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
          </div>
        </div>
        <div class="address-actions">
          <button class="btn-sm primary" @click="setDefault(addr)" v-if="addr.isDefault !== 1">设为默认</button>
          <button class="btn-sm warning" @click="editAddress(addr)">编辑</button>
          <button class="btn-sm danger" @click="deleteAddress(addr.id)">删除</button>
        </div>
      </div>
      <div v-if="addresses.length === 0" class="no-address">
        暂无收货地址，请添加
      </div>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <div class="dialog-mask" v-if="showAddDialog" @click="showAddDialog=false"></div>
    <div class="dialog" v-if="showAddDialog">
      <div class="dialog-head">
        <h3>{{ editingAddress ? '编辑地址' : '新增地址' }}</h3>
        <button @click="closeDialog" class="close">×</button>
      </div>
      <div class="dialog-body">
        <div class="form-item">
          <label>收货人姓名</label>
          <input v-model="form.name" placeholder="请输入姓名" />
        </div>
        <div class="form-item">
          <label>联系电话</label>
          <input v-model="form.phone" placeholder="请输入电话" />
        </div>
        <div class="form-item">
          <label>省份</label>
          <input v-model="form.province" placeholder="如：广东省" />
        </div>
        <div class="form-item">
          <label>城市</label>
          <input v-model="form.city" placeholder="如：深圳市" />
        </div>
        <div class="form-item">
          <label>区/县</label>
          <input v-model="form.district" placeholder="如：南山区" />
        </div>
        <div class="form-item">
          <label>详细地址</label>
          <input v-model="form.detail" placeholder="街道、楼栋、门牌号等" />
        </div>
        <div class="form-item">
          <label>
            <input type="checkbox" v-model="form.isDefault" :true-value="1" :false-value="0" />
            设为默认地址
          </label>
        </div>
      </div>
      <div class="dialog-footer">
        <button @click="closeDialog" class="btn default">取消</button>
        <button @click="submitAddress" class="btn primary">确认</button>
      </div>
    </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return {
      addresses: [],
      showAddDialog: false,
      editingAddress: null,
      form: {
        name: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detail: '',
        isDefault: 0
      }
    }
  },
  methods: {
    goBack() {
      this.$router.back()
    },
    getToken() {
      return localStorage.getItem('token')
    },
    async loadAddresses() {
      const token = this.getToken()
      if (!token) {
        alert('请先登录')
        this.$router.push('/auth')
        return
      }
      try {
        const { data } = await axios.get('http://localhost:8080/address/list', {
          headers: { Authorization: token }
        })
        console.log('地址列表响应:', data)
        if (data.success) {
          this.addresses = data.data || []
        } else {
          alert(data.message || '获取地址失败')
          if (data.message && data.message.includes('Token')) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            this.$router.push('/auth')
          }
        }
      } catch (error) {
        console.error('获取地址失败', error)
        alert('获取地址失败: ' + (error.response?.data?.message || error.message))
      }
    },
    editAddress(addr) {
      this.editingAddress = addr
      this.form = { ...addr }
      this.showAddDialog = true
    },
    closeDialog() {
      this.showAddDialog = false
      this.editingAddress = null
      this.form = { name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 }
    },
    async submitAddress() {
      const token = this.getToken()
      if (!token) {
        alert('请先登录')
        return
      }
      
      if (!this.form.name || !this.form.phone || !this.form.detail) {
        alert('请填写完整信息')
        return
      }
      
      try {
        let response
        if (this.editingAddress) {
          response = await axios.post('http://localhost:8080/address/update', this.form, {
            headers: { Authorization: token }
          })
        } else {
          response = await axios.post('http://localhost:8080/address/add', this.form, {
            headers: { Authorization: token }
          })
        }
        const data = response.data
        console.log('提交地址响应:', data)
        if (data.success) {
          alert(data.message || (this.editingAddress ? '更新成功' : '添加成功'))
          this.closeDialog()
          this.loadAddresses()
        } else {
          alert(data.message || '操作失败')
        }
      } catch (error) {
        console.error('操作失败', error)
        alert('操作失败: ' + (error.response?.data?.message || error.message))
      }
    },
    async setDefault(addr) {
      const token = this.getToken()
      if (!token) return
      
      try {
        await axios.post(`http://localhost:8080/address/setDefault/${addr.id}`, {}, {
          headers: { Authorization: token }
        })
        alert('设置成功')
        this.loadAddresses()
      } catch (error) {
        alert('设置失败')
      }
    },
    async deleteAddress(id) {
      if (!confirm('确定删除该地址？')) return
      
      const token = this.getToken()
      if (!token) return
      
      try {
        await axios.post(`http://localhost:8080/address/delete/${id}`, {}, {
          headers: { Authorization: token }
        })
        alert('删除成功')
        this.loadAddresses()
      } catch (error) {
        alert('删除失败')
      }
    }
  },
  mounted() {
    this.loadAddresses()
  }
}
</script>

<style scoped>
.address-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.address-panel {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  max-width: 800px;
  margin: 0 auto;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  background: #f5f7fa;
  color: #409eff;
  border: 1px solid #409eff;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: #409eff;
  color: #fff;
}

.address-header h3 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.add-btn {
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-item.default {
  background: linear-gradient(135deg, #ecf5ff 0%, #e6f0ff 100%);
  border: 2px solid #409eff;
}

.address-info {
  flex: 1;
}

.address-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.address-phone {
  color: #666;
  margin-left: 12px;
}

.default-tag {
  background: #409eff;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 12px;
}

.address-detail {
  color: #666;
  font-size: 14px;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.btn-sm {
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 13px;
}

.btn-sm.primary { background: #409eff; color: #fff; }
.btn-sm.warning { background: #e6a23c; color: #fff; }
.btn-sm.danger { background: #f56c6c; color: #fff; }

.no-address {
  text-align: center;
  color: #999;
  padding: 40px;
}

/* 弹窗样式 */
.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  z-index: 1000;
}

.dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%,-50%);
  width: 400px;
  background: #fff;
  border-radius: 16px;
  z-index: 1001;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.dialog-head {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e6eb;
}

.dialog-head h3 {
  margin: 0;
  color: #333;
}

.close {
  border: none;
  background: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.dialog-body {
  padding: 20px;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 6px;
  color: #333;
  font-weight: 500;
}

.form-item input {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid #e5e6eb;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-item input:focus {
  outline: none;
  border-color: #409eff;
}

.dialog-footer {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e5e6eb;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 14px;
}

.btn.primary { background: #409eff; color: #fff; }
.btn.default { background: #f5f7fa; color: #333; }
</style>