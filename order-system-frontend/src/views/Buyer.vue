<template>
  <div class="container">
    <h2 class="page-title">在线点餐商城</h2>

    <!-- 购物车悬浮按钮 -->
    <button class="cart-btn" @click="showCartDrawer = true">
      🛒 购物车
      <span v-if="totalItems > 0" class="badge">{{ totalItems }}</span>
    </button>

    <!-- 分类标签栏 -->
    <div class="category-tabs">
      <button :class="{active: activeCat === ''}" @click="getProducts()">全部商品</button>
      <button :class="{active: activeCat === '饮料'}" @click="getByCategory('饮料')">饮料</button>
      <button :class="{active: activeCat === '零食'}" @click="getByCategory('零食')">零食</button>
      <button :class="{active: activeCat === '主食'}" @click="getByCategory('主食')">主食</button>
      <button :class="{active: activeCat === '水果'}" @click="getByCategory('水果')">水果</button>
      <button :class="{active: activeCat === '五金'}" @click="getByCategory('五金')">五金</button>
      <button :class="{active: activeCat === '文具'}" @click="getByCategory('文具')">文具</button>
    </div>

    <!-- 商品列表 -->
    <div class="product-list">
      <div class="product-card" v-for="product in products" :key="product.id">
        <!-- 核心：自动匹配Emoji -->
        <div class="product-icon">
          {{ getProductEmoji(product.name) }}
        </div>
        <h3 class="product-name">{{ product.name }}</h3>
        <p class="price">¥{{ product.price }}</p>
        <p class="stock" :class="{'low':product.stock < 5}">库存：{{ product.stock }}</p>

        <div class="num-control">
          <button @click="dec(product.id)">-</button>
          <span>{{ cart[product.id] || 0 }}</span>
          <button @click="add(product.id, product.stock)">+</button>
        </div>

        <input v-model="remarkList[product.id]" placeholder="下单备注（选填）" class="remark-input" />
      </div>
    </div>

    <!-- 我的历史订单 -->
    <div class="order-wrap">
      <h3 class="order-title">📋 我的历史订单</h3>
      <div class="order-list">
        <div v-for="item in myOrders" :key="item.id" class="order-item">
          <div class="order-left">
            <span>订单编号：{{ item.id }}</span>
            <span>状态：{{ item.status }}</span>
          </div>
          <div class="order-right">备注：{{ item.remark || '无' }}</div>
        </div>
        <div v-if="myOrders.length === 0" class="empty-tip">暂无下单记录</div>
      </div>
    </div>

    <!-- 购物车遮罩 -->
    <div class="overlay" v-if="showCartDrawer" @click="showCartDrawer=false"></div>
    <!-- 购物车侧边抽屉 -->
    <div class="drawer" :class="{open:showCartDrawer}">
      <div class="drawer-head">
        <h3>我的购物车</h3>
        <button @click="showCartDrawer=false" class="close-btn">×</button>
      </div>
      <div class="drawer-body">
        <div v-if="totalItems === 0" class="cart-empty">购物车空空如也～</div>
        <div v-else>
          <div v-for="(qty, pid) in cart" :key="pid" class="cart-item">
            <div class="cart-item-top">
              <span class="cart-name">{{ getName(pid) }}</span>
              <span>× {{ qty }}</span>
              <span class="cart-price">¥{{ getPrice(pid)*qty }}</span>
            </div>
            <div v-if="remarkList[pid]" class="cart-remark">备注：{{ remarkList[pid] }}</div>
          </div>
          <div class="cart-total">合计：¥{{ totalPrice.toFixed(2) }}</div>
          <button class="submit-btn" @click="submit()">立即结算下单</button>
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
      products: [],
      myOrders: [],
      cart: {},
      remarkList: {},
      showCartDrawer: false,
      activeCat: ''
    }
  },
  computed: {
    totalItems() {
      return Object.values(this.cart).reduce((a,b)=>a+b,0)
    },
    totalPrice() {
      return Object.entries(this.cart).reduce((sum,[pid,qty])=>{
        const p = this.products.find(x=>x.id==pid)
        return sum + (p?p.price*qty:0)
      },0)
    }
  },
  methods: {
    // 关键方法：自动根据商品名匹配Emoji
    getProductEmoji(name){
      // 水果类
      if(name.includes('苹果')) return '🍎'
      if(name.includes('香蕉')) return '🍌'
      if(name.includes('橙子') || name.includes('橘子')) return '🍊'
      if(name.includes('葡萄')) return '🍇'
      if(name.includes('西瓜')) return '🍉'
      if(name.includes('芒果')) return '🥭'

      // 饮品类
      if(name.includes('可乐') || name.includes('饮料')) return '🥤'
      if(name.includes('牛奶')) return '🥛'
      if(name.includes('矿泉水') || name.includes('水')) return '💧'
      if(name.includes('奶茶')) return '🧋'

      // 零食类
      if(name.includes('面包')) return '🍞'
      if(name.includes('薯片')) return '🥔'
      if(name.includes('饼干')) return '🍪'
      if(name.includes('火腿肠')) return '🌭'
      if(name.includes('方便面') || name.includes('泡面')) return '🍜'

      // 五金工具类
      if(name.includes('螺丝') || name.includes('螺母')) return '🔩'
      if(name.includes('扳手') || name.includes('钳子')) return '🔧'
      if(name.includes('锤子')) return '🔨'

      // 文具类
      if(name.includes('笔')) return '✏️'
      if(name.includes('本子') || name.includes('笔记本')) return '📒'
      if(name.includes('书包')) return '🎒'

      // 默认图标
      return '📦'
    },

    async getProducts() {
      const {data} = await axios.get('http://localhost:8080/order/product/list')
      this.products = data
      this.activeCat = ''
    },
    async getByCategory(cat) {
      const {data} = await axios.get('http://localhost:8080/order/product/category?category='+cat)
      this.products = data
      this.activeCat = cat
    },
    async getMyOrders() {
      const {data} = await axios.get('http://localhost:8080/order/user/order')
      this.myOrders = data
    },
    add(pid, max) {
      const now = this.cart[pid] || 0
      if (now < max) this.cart[pid] = now + 1
    },
    dec(pid) {
      const now = this.cart[pid] || 0
      if (now > 0) {
        this.cart[pid]--
        if (this.cart[pid] === 0) {
          delete this.cart[pid]
          delete this.remarkList[pid]
        }
      }
    },
    getName(pid) {
      return this.products.find(x=>x.id==pid)?.name || ''
    },
    getPrice(pid) {
      return this.products.find(x=>x.id==pid)?.price || 0
    },
    async submit() {
      const list = Object.entries(this.cart).map(([pid,qty])=>({
        productId: parseInt(pid), quantity: qty, remark: this.remarkList[pid]||''
      }))
      await axios.post('http://localhost:8080/order/batchAdd', list)
      alert('🎉 下单成功！')
      this.cart = {}
      this.remarkList = {}
      this.showCartDrawer = false
      this.getProducts()
      this.getMyOrders()
    }
  },
  mounted() {
    this.getProducts()
    this.getMyOrders()
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
.container{
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  background: #f5f7fa;
  min-height: 100vh;
}
.page-title{
  text-align: center;
  font-size: 28px;
  color: #2a3342;
  margin-bottom: 35px;
  font-weight: 600;
}

.cart-btn{
  position: fixed;
  top: 30px;
  right: 30px;
  background: #409eff;
  color: #fff;
  border: none;
  padding: 12px 20px;
  border-radius: 30px;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(64,158,255,0.3);
  z-index: 99;
  transition: all 0.3s;
}
.cart-btn:hover{
  background: #337ecc;
}
.badge{
  background: #f56c6c;
  color: #fff;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  margin-left: 8px;
}

.category-tabs{
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 35px;
  flex-wrap: wrap;
}
.category-tabs button{
  padding: 10px 22px;
  border: 1px solid #dcdfe6;
  border-radius: 25px;
  background: #fff;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s;
}
.category-tabs button.active{
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}
.category-tabs button:hover:not(.active){
  border-color: #409eff;
  color: #409eff;
}

.product-list{
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  justify-content: center;
}
.product-card{
  width: 220px;
  background: #fff;
  border-radius: 16px;
  padding: 25px 20px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0,0,0,0.06);
  transition: transform 0.3s, box-shadow 0.3s;
}
.product-card:hover{
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}
.product-icon{
  font-size: 48px;
  margin-bottom: 15px;
}
.product-name{
  font-size: 17px;
  color: #333;
  margin-bottom: 8px;
}
.price{
  color: #f56c6c;
  font-size: 19px;
  font-weight: 600;
  margin-bottom: 6px;
}
.stock{
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}
.stock.low{
  color: #f56c6c;
  font-weight: 500;
}

.num-control{
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
}
.num-control button{
  width: 32px;
  height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #f5f7fa;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
}
.num-control button:hover{
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.remark-input{
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border 0.3s;
}
.remark-input:focus{
  border-color: #409eff;
}

.order-wrap{
  max-width: 900px;
  margin: 60px auto 0;
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.06);
}
.order-title{
  font-size: 20px;
  color: #2a3342;
  margin-bottom: 20px;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}
.order-item{
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px dashed #eee;
  color: #555;
}
.empty-tip{
  text-align: center;
  padding: 30px;
  color: #999;
}

.overlay{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 999;
}
.drawer{
  position: fixed;
  top: 0;
  right: -380px;
  width: 380px;
  height: 100vh;
  background: #fff;
  z-index: 1000;
  transition: right 0.3s ease;
  box-shadow: -5px 0 20px rgba(0,0,0,0.1);
}
.drawer.open{
  right: 0;
}
.drawer-head{
  padding: 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  font-size: 18px;
  font-weight: 600;
}
.close-btn{
  border: none;
  background: none;
  font-size: 26px;
  cursor: pointer;
  color: #999;
}
.drawer-body{
  padding: 25px;
  height: calc(100% - 70px);
  overflow-y: auto;
}
.cart-empty{
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
}
.cart-item{
  margin-bottom: 18px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #eee;
}
.cart-item-top{
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cart-name{
  font-weight: 500;
}
.cart-price{
  color: #f56c6c;
  font-weight: 600;
}
.cart-remark{
  font-size: 13px;
  color: #888;
  margin-top: 6px;
  padding-left: 4px;
}
.cart-total{
  text-align: right;
  font-size: 18px;
  font-weight: 600;
  margin: 25px 0;
  color: #333;
}
.submit-btn{
  width: 100%;
  background: #409eff;
  color: #fff;
  border: none;
  padding: 12px;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}
.submit-btn:hover{
  background: #337ecc;
}
</style>