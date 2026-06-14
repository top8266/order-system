<template>
  <div class="container">
    <header class="page-header">
      <h1 class="page-title">🏪 商家管理后台</h1>
    </header>

    <!-- 库存预警卡片 -->
    <div v-if="warningList.length > 0" class="warning-card">
      <div class="warning-header">
        <span class="warning-icon">⚠️</span>
        <h4>库存预警</h4>
      </div>
      <div class="warning-content">
        <div v-for="item in warningList" :key="item.id" class="warning-item">
          <span class="warning-name">{{ item.emoji }} {{ item.name }}</span>
          <span class="warning-stock">库存: <span class="red-text">{{ item.stock }}</span></span>
          <button class="restock-btn" @click="openRestock(item)">补货</button>
        </div>
      </div>
    </div>

    <!-- 商品管理区域 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">📦 商品管理</h2>
        <button class="add-goods-btn" @click="showAddDialog=true">+ 新增商品</button>
      </div>
      
      <!-- 分类筛选 -->
      <div class="category-filter">
        <button :class="{active: selectedCategory === ''}" @click="selectedCategory = ''">全部</button>
        <button v-for="cat in categoryList" :key="cat" :class="{active: selectedCategory === cat}" @click="selectedCategory = cat">{{ cat }}</button>
      </div>

      <!-- 商品列表 -->
      <div class="goods-list-container">
        <div class="goods-list">
          <div v-for="item in filteredGoods" :key="item.id" class="goods-item">
            <span class="goods-emoji">{{ item.emoji }}</span>
            <div class="goods-info">
              <span class="goods-name">{{ item.name }}</span>
              <span class="goods-price">¥{{ item.price.toFixed(2) }}</span>
              <span class="goods-stock">库存: {{ item.stock }}</span>
              <span class="goods-category">{{ item.category }}</span>
            </div>
            <div class="goods-actions">
              <button class="action-btn edit" @click="openEdit(item)">编辑</button>
              <button class="action-btn delete" @click="delGoods(item.id)">删除</button>
              <button class="action-btn restock" @click="openRestock(item)">补货</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 订单统计 -->
    <div class="stat-wrap">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-num">{{ orders.length }}</div>
          <div class="stat-label">全部订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏳</div>
        <div class="stat-content">
          <div class="stat-num wait">{{ orders.filter(o=>o.status=='待接单').length }}</div>
          <div class="stat-label">待接单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <div class="stat-num prepare">{{ orders.filter(o=>o.status=='备货中').length }}</div>
          <div class="stat-label">备货中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-num ok">{{ orders.filter(o=>o.status=='已送达').length }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- 订单列表按钮 -->
    <button class="order-toggle-btn" @click="showOrderList = !showOrderList">
      <span class="toggle-icon">{{ showOrderList ? '▼' : '▶' }}</span>
      {{ showOrderList ? '收起订单列表' : '展开订单列表' }}
    </button>

    <!-- 订单列表（可折叠） -->
    <div v-if="showOrderList" class="order-panel">
      <div class="tool-bar">
        <button class="btn primary" @click="selectAll">全选</button>
        <button class="btn success" @click="batchComplete">批量完成</button>
        <button class="btn danger" @click="batchDelete">批量删除</button>
        <button class="btn default" @click="refreshAll">刷新数据</button>
      </div>

      <div class="table-container">
        <h3 class="table-title">📋 订单列表管理</h3>
        <div class="order-group-list">
          <div v-for="(group, orderNo) in groupedOrders" :key="orderNo" class="order-group-card">
            <div class="order-group-header">
              <div class="order-info">
                <span class="order-no-label">订单号：{{ orderNo }}</span>
                <span class="order-status-tag" :class="getStatusClass(group[0].status)">
                  {{ group[0].status }}
                </span>
              </div>
              <div class="order-actions">
                <button v-if="group[0].status=='待接单'" class="btn-sm success" @click="confirmOrderByGroup(group)">确认订单</button>
                <button v-if="group[0].status=='备货中'" class="btn-sm primary" @click="readyOrderByGroup(group)">备货完成</button>
                <button v-if="group[0].status=='待接单' || group[0].status=='备货中' || group[0].status=='待配送'" class="btn-sm danger" @click="delByGroup(group)">删除订单</button>
              </div>
            </div>
            <div class="order-group-body">
              <div class="order-products">
                <div v-for="item in group" :key="item.id" class="order-product-row">
                  <span class="product-name">{{ item.productName || '商品#' + item.productId }}</span>
                  <span class="product-quantity">×{{ item.quantity || 1 }}</span>
                  <span class="product-price">¥{{ (item.productPrice || 0).toFixed(2) }}</span>
                </div>
              </div>
              <div class="order-address">
                <span class="address-label">📍 收货地址：</span>
                <span class="address-content">{{ group[0].address || '未填写' }}</span>
              </div>
              <div class="order-user-phone">
                <span class="phone-label">📞 用户电话：</span>
                <span class="phone-content">{{ group[0].userPhone || '未填写' }}</span>
              </div>
              <div v-if="group[0].remark" class="order-remark">
                <span class="remark-label">用户备注：</span>
                <span class="remark-content">{{ group[0].remark }}</span>
              </div>
              <div class="order-total">
                <span class="total-label">订单总额：</span>
                <span class="total-amount">¥{{ getOrderTotal(group).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增商品弹窗 -->
    <div class="dialog-mask" v-if="showAddDialog" @click="showAddDialog=false"></div>
    <div class="dialog" v-if="showAddDialog">
      <div class="dialog-head">
        <h3>新增商品</h3>
        <button @click="showAddDialog=false" class="close">×</button>
      </div>
      <div class="dialog-body">
        <div class="form-item">
          <label>商品名称</label>
          <input v-model="newGoods.name" placeholder="请输入商品名称" />
        </div>
        <div class="form-item">
          <label>商品价格</label>
          <input v-model.number="newGoods.price" type="number" placeholder="请输入价格" />
        </div>
        <div class="form-item">
          <label>初始库存</label>
          <input v-model.number="newGoods.stock" type="number" placeholder="请输入初始库存" />
        </div>
        <div class="form-item">
          <label>商品分类</label>
          <input
            list="categoryOptions"
            v-model="newGoods.category"
            placeholder="选择或输入分类：水果/五金/文具等"
          />
          <datalist id="categoryOptions">
            <option value="饮料">饮料</option>
            <option value="零食">零食</option>
            <option value="水果">水果</option>
            <option value="文具">文具</option>
            <option value="五金">五金</option>
            <option value="日用品">日用品</option>
          </datalist>
        </div>
        <div class="form-item">
          <label>商品图标（Emoji）</label>
          <input v-model="newGoods.emoji" placeholder="输入Emoji表情，如 🍎" />
        </div>
      </div>
      <div class="dialog-footer">
        <button @click="showAddDialog=false" class="btn default">取消</button>
        <button @click="submitAdd" class="btn primary">确认添加</button>
      </div>
    </div>

    <!-- 编辑商品弹窗 -->
    <div class="dialog-mask" v-if="showEditDialog" @click="showEditDialog=false"></div>
    <div class="dialog" v-if="showEditDialog">
      <div class="dialog-head">
        <h3>编辑商品</h3>
        <button @click="showEditDialog=false" class="close">×</button>
      </div>
      <div class="dialog-body">
        <div class="form-item">
          <label>商品名称</label>
          <input v-model="editGoods.name" placeholder="请输入商品名称" />
        </div>
        <div class="form-item">
          <label>商品价格</label>
          <input v-model.number="editGoods.price" type="number" placeholder="请输入价格" />
        </div>
        <div class="form-item">
          <label>库存</label>
          <input v-model.number="editGoods.stock" type="number" placeholder="请输入库存" />
        </div>
        <div class="form-item">
          <label>商品分类</label>
          <input
            list="categoryOptions"
            v-model="editGoods.category"
            placeholder="选择或输入分类"
          />
        </div>
        <div class="form-item">
          <label>商品图标（Emoji）</label>
          <input v-model="editGoods.emoji" placeholder="输入Emoji表情" />
        </div>
      </div>
      <div class="dialog-footer">
        <button @click="showEditDialog=false" class="btn default">取消</button>
        <button @click="submitEdit" class="btn primary">确认修改</button>
      </div>
    </div>

    <!-- 补货弹窗 -->
    <div class="dialog-mask" v-if="showRestockDialog" @click="showRestockDialog=false"></div>
    <div class="dialog" v-if="showRestockDialog">
      <div class="dialog-head">
        <h3>补货 - {{ currGoods.name }}</h3>
        <button @click="showRestockDialog=false" class="close">×</button>
      </div>
      <div class="dialog-body">
        <div class="form-item">
          <label>当前库存</label>
          <input :value="currGoods.stock" disabled />
        </div>
        <div class="form-item">
          <label>补货数量</label>
          <input v-model.number="addStockNum" type="number" placeholder="请输入补货数量" />
        </div>
      </div>
      <div class="dialog-footer">
        <button @click="showRestockDialog=false" class="btn default">取消</button>
        <button @click="submitRestock" class="btn success">确认补货</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return {
      orders: [],
      goodsList: [],
      warningList: [],
      selected: [],
      showOrderList: false,

      showAddDialog: false,
      newGoods: {
        name: '',
        price: 0,
        stock: 0,
        category: '',
        emoji: ''
      },

      showRestockDialog: false,
      currGoods: {},
      addStockNum: 0,

      showEditDialog: false,
      editGoods: {
        id: null,
        name: '',
        price: 0,
        stock: 0,
        category: '',
        emoji: ''
      },

      selectedCategory: '',
      categoryList: []
    }
  },
  computed: {
    filteredGoods() {
      if (!this.selectedCategory) {
        return this.goodsList
      }
      return this.goodsList.filter(item => item.category === this.selectedCategory)
    },
    groupedOrders() {
      const groups = {}
      this.orders.forEach(order => {
        const orderNo = order.orderNo || order.id
        if (!groups[orderNo]) {
          groups[orderNo] = []
        }
        groups[orderNo].push(order)
      })
      return groups
    }
  },
  methods: {
    getStatusClass(status) {
      switch(status) {
        case '待接单': return 'tag-wait'
        case '备货中': return 'tag-prepare'
        case '待配送': return 'tag-delivery'
        case '已接单': return 'tag-accepted'
        case '配送中': return 'tag-shipping'
        case '已送达': return 'tag-ok'
        default: return 'tag-default'
      }
    },
    async refreshAll() {
      await this.getGoodsList()
      await this.getOrderList()
    },
    async getGoodsList() {
      const userStr = localStorage.getItem('user')
      const sellerId = userStr ? JSON.parse(userStr).id : null
      
      const url = sellerId ? `http://localhost:8080/order/product/list?sellerId=${sellerId}` : 'http://localhost:8080/order/product/list'
      const {data} = await axios.get(url)
      this.goodsList = data
      const categories = new Set()
      data.forEach(item => {
        if(item.category) categories.add(item.category)
      })
      this.categoryList = Array.from(categories)
      
      const warnUrl = sellerId ? `http://localhost:8080/order/product/warning?sellerId=${sellerId}` : 'http://localhost:8080/order/product/warning'
      const {data:warn} = await axios.get(warnUrl)
      this.warningList = warn
    },
    async getOrderList() {
      const userStr = localStorage.getItem('user')
      const sellerId = userStr ? JSON.parse(userStr).id : null
      const url = sellerId ? `http://localhost:8080/order/list?sellerId=${sellerId}` : 'http://localhost:8080/order/list'
      const {data} = await axios.get(url)
      this.orders = data
      this.selected = []
    },

    openRestock(item) {
      this.currGoods = {...item}
      this.addStockNum = 0
      this.showRestockDialog = true
    },
    async submitRestock() {
      if(!this.addStockNum || this.addStockNum <= 0){
        alert('请输入合法的库存数量')
        return
      }
      await axios.post('http://localhost:8080/order/product/restock',{
        id: this.currGoods.id,
        stock: this.addStockNum
      })
      alert('补货成功')
      this.showRestockDialog = false
      this.refreshAll()
    },

    openEdit(item) {
      this.editGoods = { ...item }
      this.showEditDialog = true
    },
    async submitEdit() {
      if(!this.editGoods.name){
        alert('请输入商品名称')
        return
      }
      await axios.post('http://localhost:8080/order/product/update', this.editGoods)
      alert('修改成功')
      this.showEditDialog = false
      this.refreshAll()
    },

    async submitAdd() {
      if(!this.newGoods.name){
        alert('请输入商品名称')
        return
      }
      await axios.post('http://localhost:8080/order/product/add', this.newGoods)
      alert('添加成功')
      this.showAddDialog = false
      this.newGoods = { name: '', price: 0, stock: 0, category: '', emoji: '' }
      this.refreshAll()
    },

    async delGoods(id) {
      if(confirm('确定要删除该商品？')){
        await axios.delete(`http://localhost:8080/order/product/${id}`)
        alert('删除成功')
        this.refreshAll()
      }
    },

    selectAll() {
      if(this.selected.length === this.orders.length){
        this.selected = []
      }else{
        this.selected = this.orders.map(o => o.id)
      }
    },
    async complete(id) {
      await axios.post('http://localhost:8080/order/complete', {id})
      this.getOrderList()
    },
    async batchComplete() {
      if(this.selected.length === 0){
        alert("请先选择订单")
        return
      }
      await axios.post('http://localhost:8080/order/batchComplete', this.selected)
      this.refreshAll()
      alert("批量完成成功")
    },
    async confirmOrder(orderId) {
      if(confirm('确认订单？订单将进入备货状态')){
        await axios.post('http://localhost:8080/order/status/update', {
          orderNo: this.orders.find(o=>o.id===orderId)?.orderNo,
          status: '备货中'
        })
        alert('订单已确认，开始备货')
        this.refreshAll()
      }
    },
    async readyOrder(orderId) {
      if(confirm('备货完成？订单将变为待配送状态，等待配送员接单')){
        await axios.post('http://localhost:8080/order/status/update', {
          orderNo: this.orders.find(o=>o.id===orderId)?.orderNo,
          status: '待配送'
        })
        alert('备货完成，等待配送员接单')
        this.refreshAll()
      }
    },
    async batchDelete() {
      if(this.selected.length === 0){
        alert("请先选择订单")
        return
      }
      if(confirm('确定要删除选中订单？')){
        await axios.post('http://localhost:8080/order/batchDelete', this.selected)
        this.refreshAll()
        alert("批量删除成功")
      }
    },
    
    getOrderTotal(orderGroup) {
      return orderGroup.reduce((total, item) => {
        return total + (item.productPrice || 0) * (item.quantity || 1)
      }, 0)
    },
    
    async confirmOrderByGroup(orderGroup) {
      if(confirm('确认订单？订单将进入备货状态')){
        await axios.post('http://localhost:8080/order/status/update', {
          orderNo: orderGroup[0].orderNo,
          status: '备货中'
        })
        alert('订单已确认，开始备货')
        this.refreshAll()
      }
    },
    
    async readyOrderByGroup(orderGroup) {
      if(confirm('备货完成？订单将变为待配送状态，等待配送员接单')){
        await axios.post('http://localhost:8080/order/status/update', {
          orderNo: orderGroup[0].orderNo,
          status: '待配送'
        })
        alert('备货完成，等待配送员接单')
        this.refreshAll()
      }
    },
    
    async delByGroup(orderGroup) {
      if(confirm('确定要删除该订单？')){
        const ids = orderGroup.map(o => o.id)
        await axios.post('http://localhost:8080/order/batchDelete', ids)
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
/* 全局容器 - 白色主题 */
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
.page-subtitle{
  font-size: 16px;
  color: #666;
}

/* 警告卡片 */
.warning-card{
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 30px;
  border: 1px solid #ffcccc;
  box-shadow: 0 8px 32px rgba(245, 108, 108, 0.15);
}
.warning-header{
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.warning-icon{
  font-size: 24px;
}
.warning-header h4{
  color: #c03636;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}
.warning-content{
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.warning-item{
  background: #fff;
  padding: 12px 16px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.warning-name{
  font-weight: 500;
  color: #333;
}
.warning-stock{
  color: #666;
}
.red-text{
  color: #f56c6c;
  font-weight: 600;
}
.restock-btn{
  background: #f56c6c;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}
.restock-btn:hover{
  background: #e45a5a;
  transform: translateY(-2px);
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

/* 按钮样式 */
.add-goods-btn{
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
.add-goods-btn:hover{
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
}

/* 分类筛选 */
.category-filter{
  display: flex;
  gap: 12px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}
.category-filter button{
  background: #f5f7fa;
  color: #606266;
  border: 2px solid transparent;
  padding: 10px 20px;
  border-radius: 25px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}
.category-filter button:hover{
  background: #ecf5ff;
  border-color: #b3d8ff;
}
.category-filter button.active{
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* 商品列表容器 */
.goods-list-container{
  background: #fafafa;
  border-radius: 12px;
  padding: 4px;
}
.goods-list{
  max-height: 400px;
  overflow-y: auto;
}
.goods-list::-webkit-scrollbar{
  width: 6px;
}
.goods-list::-webkit-scrollbar-track{
  background: #f1f1f1;
  border-radius: 3px;
}
.goods-list::-webkit-scrollbar-thumb{
  background: #c1c1c1;
  border-radius: 3px;
}
.goods-list::-webkit-scrollbar-thumb:hover{
  background: #a8a8a8;
}
.goods-item{
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #eee;
  transition: background 0.3s;
}
.goods-item:last-child{
  border-bottom: none;
}
.goods-item:hover{
  background: #fafafa;
}
.goods-emoji{
  font-size: 32px;
  width: 50px;
  text-align: center;
}
.goods-info{
  flex: 1;
  display: flex;
  gap: 20px;
  align-items: center;
}
.goods-name{
  font-weight: 600;
  color: #2a3342;
  min-width: 100px;
}
.goods-price{
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}
.goods-stock{
  color: #666;
  font-size: 14px;
}
.goods-category{
  display: inline-block;
  background: #f0f5ff;
  color: #409eff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}
.goods-actions{
  display: flex;
  gap: 8px;
}
.action-btn{
  padding: 6px 12px;
  border: 1px solid #333;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
  background: #fff;
  color: #333;
}
.action-btn:hover{
  background: #333;
  color: #fff;
}
.action-btn.edit:hover{
  background: #409eff;
  border-color: #409eff;
}
.action-btn.delete:hover{
  background: #f56c6c;
  border-color: #f56c6c;
}
.action-btn.restock:hover{
  background: #67c23a;
  border-color: #67c23a;
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
.stat-num.wait{
  color: #e6a23c;
}
.stat-num.prepare{
  color: #409eff;
}
.stat-num.ok{
  color: #67c23a;
}
.stat-label{
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

/* 订单切换按钮 */
.order-toggle-btn{
  width: 100%;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
  border: none;
  padding: 18px 30px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  transition: all 0.3s;
  box-shadow: 0 8px 25px rgba(17, 153, 142, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.order-toggle-btn:hover{
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(17, 153, 142, 0.5);
}
.toggle-icon{
  font-size: 20px;
}

/* 订单面板 */
.order-panel{
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  animation: slideDown 0.4s ease;
  max-height: none;
  overflow: visible;
}
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 工具栏 */
.tool-bar{
  display: flex;
  gap: 12px;
  margin-bottom: 25px;
  flex-wrap: wrap;
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
.btn.success{
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.4);
}
.btn.success:hover{
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.5);
}
.btn.danger{
  background: linear-gradient(135deg, #f56c6c 0%, #f87171 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(245, 108, 108, 0.4);
}
.btn.danger:hover{
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.5);
}
.btn.default{
  background: #f5f7fa;
  color: #606266;
}
.btn.default:hover{
  background: #e8eaed;
}

/* 表格容器 */
.table-container{
  background: #fafafa;
  border-radius: 16px;
  padding: 24px;
  max-height: none;
  overflow: visible;
}
.table-title{
  font-size: 18px;
  font-weight: 600;
  color: #2a3342;
  margin: 0 0 20px 0;
}
.table-scroll{
  max-height: 400px;
  overflow-y: auto;
  border-radius: 12px;
}
.table-scroll::-webkit-scrollbar{
  width: 8px;
}
.table-scroll::-webkit-scrollbar-track{
  background: #f1f1f1;
  border-radius: 4px;
}
.table-scroll::-webkit-scrollbar-thumb{
  background: #c1c1c1;
  border-radius: 4px;
}
.table-scroll::-webkit-scrollbar-thumb:hover{
  background: #a8a8a8;
}

/* 订单表格 */
.order-table{
  width: 100%;
  border-collapse: collapse;
}
.order-table th, .order-table td{
  padding: 16px 12px;
  text-align: left;
  border-bottom: 1px solid #e5e6eb;
}
.order-table th{
  background: #f5f7fa;
  font-weight: 600;
  color: #606266;
  position: sticky;
  top: 0;
  z-index: 1;
}
.order-table tr:hover{
  background: #fafafa;
}
.check-box{
  cursor: pointer;
  width: 18px;
  height: 18px;
}
.order-no-cell{
  font-family: monospace;
  font-size: 13px;
  color: #666;
}

/* 状态标签 */
.tag{
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}
.tag-wait{
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}
.tag-prepare{
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  color: #409eff;
}
.tag-delivery{
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #67c23a;
}
.tag-accepted{
  background: linear-gradient(135deg, #fff7e6 0%, #ffeeba 100%);
  color: #fa9800;
}
.tag-shipping{
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #1890ff;
}
.tag-ok{
  background: linear-gradient(135deg, #f0f9eb 0%, #d4edda 100%);
  color: #67c23a;
}
.tag-default{
  background: #f5f5f5;
  color: #999;
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
  color: #999;
  transition: color 0.3s;
}
.close:hover{
  color: #666;
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
.form-item input{
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e5e6eb;
  border-radius: 10px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}
.form-item input:focus{
  outline: none;
  border-color: #409eff;
}
.form-item input:disabled{
  background: #f5f7fa;
  color: #999;
}
.dialog-footer{
  padding: 20px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e5e6eb;
}

/* 响应式 */
@media (max-width: 768px) {
  .container{
    padding: 20px 15px;
  }
  .page-title{
    font-size: 28px;
  }
  .goods-grid{
    grid-template-columns: 1fr;
  }
  .stat-wrap{
    grid-template-columns: repeat(2, 1fr);
  }
  .dialog{
    width: 90%;
    max-width: 400px;
  }
  .order-table{
    font-size: 13px;
  }
  .order-table th, .order-table td{
    padding: 12px 8px;
  }
}

/* 分组订单列表 */
.order-group-list{
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: calc(100vh - 280px);
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 12px;
  scrollbar-width: thin;
  scrollbar-color: #999999 #e8eaed;
}
.order-group-list::-webkit-scrollbar {
  width: 10px;
}
.order-group-list::-webkit-scrollbar-track {
  background: #e8eaed;
  border-radius: 5px;
}
.order-group-list::-webkit-scrollbar-thumb {
  background: #999999;
  border-radius: 5px;
  border: 2px solid #e8eaed;
}
.order-group-list::-webkit-scrollbar-thumb:hover {
  background: #666666;
}
.order-group-card{
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
  overflow: visible;
  transition: all 0.3s;
}
.order-group-card:hover{
  box-shadow: 0 8px 25px rgba(0,0,0,0.12);
}
.order-group-header{
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #e5e6eb;
}
.order-info{
  display: flex;
  align-items: center;
  gap: 12px;
}
.order-no-label{
  font-size: 14px;
  font-weight: 600;
  color: #333;
  font-family: monospace;
}
.order-status-tag{
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.order-status-tag.tag-wait{
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}
.order-status-tag.tag-prepare{
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  color: #409eff;
}
.order-status-tag.tag-delivery{
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #67c23a;
}
.order-status-tag.tag-accepted{
  background: linear-gradient(135deg, #fff7e6 0%, #ffeeba 100%);
  color: #fa9800;
}
.order-actions{
  display: flex;
  gap: 8px;
}
.order-group-body{
  padding: 20px;
}
.order-products{
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}
.order-product-row{
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 10px;
}
.product-name{
  font-weight: 500;
  color: #333;
  flex: 1;
}
.product-quantity{
  color: #666;
  font-size: 14px;
  margin: 0 12px;
}
.product-price{
  font-weight: 600;
  color: #f56c6c;
}
.order-address{
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background: #e6f7ff;
  border-radius: 10px;
  margin-bottom: 10px;
}
.address-label{
  color: #1890ff;
  font-size: 14px;
  margin-right: 8px;
  white-space: nowrap;
}
.address-content{
  color: #333;
  font-size: 14px;
}
.order-user-phone{
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background: #f6ffed;
  border-radius: 10px;
  margin-bottom: 10px;
}
.phone-label{
  color: #52c41a;
  font-size: 14px;
  margin-right: 8px;
  white-space: nowrap;
}
.phone-content{
  color: #333;
  font-size: 14px;
  font-weight: 500;
}
.order-remark{
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff5f5;
  border-radius: 10px;
  margin-bottom: 16px;
}
.remark-label{
  color: #999;
  font-size: 14px;
  margin-right: 8px;
}
.remark-content{
  color: #e6a23c;
  font-weight: 500;
}
.order-total{
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-top: 16px;
  border-top: 1px dashed #e5e6eb;
}
.total-label{
  color: #666;
  font-size: 14px;
  margin-right: 8px;
}
.total-amount{
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}
</style>
