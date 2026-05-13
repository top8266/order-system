<template>
  <div class="buyer-container">
    <header class="header">
      <h1>在线商城</h1>
      <div class="header-buttons">
        <button class="cart-button" @click="openCart">
          🛒 购物车
          <span class="badge" v-if="totalItems > 0">{{ totalItems }}</span>
        </button>
        <button class="order-button" @click="openOrder">📋 我的订单</button>
      </div>
    </header>

    <div class="category-box">
      <button :class="['cat-btn', active === '' ? 'active' : '']" @click="getAll">全部</button>
      <button :class="['cat-btn', active === '饮料' ? 'active' : '']" @click="getCat('饮料')">饮料</button>
      <button :class="['cat-btn', active === '零食' ? 'active' : '']" @click="getCat('零食')">零食</button>
      <button :class="['cat-btn', active === '主食' ? 'active' : '']" @click="getCat('主食')">主食</button>
      <button :class="['cat-btn', active === '水果' ? 'active' : '']" @click="getCat('水果')">水果</button>
      <button :class="['cat-btn', active === '五金' ? 'active' : '']" @click="getCat('五金')">五金</button>
      <button :class="['cat-btn', active === '文具' ? 'active' : '']" @click="getCat('文具')">文具</button>
      <button :class="['cat-btn', active === '日化' ? 'active' : '']" @click="getCat('日化')">日化</button>
    </div>

    <div class="product-scroll">
      <div class="product-grid">
        <div class="product-card" v-for="p in products" :key="p.id">
          <div class="emoji">{{ getSmartEmoji(p.name, p.id) }}</div>
          <div class="name">{{ p.name }}</div>
          <div class="price">售价：¥{{ p.price }}</div>
          <div class="stock" :class="p.stock < 5 ? 'low' : ''">库存：{{ p.stock }}</div>

          <div class="ctrl">
            <button @click="sub(p.id)">-</button>
            <span>{{ cart[p.id] || 0 }}</span>
            <button @click="add(p.id, p.stock)">+</button>
          </div>

          <input v-model="remark[p.id]" placeholder="备注（选填）" class="input" />
        </div>
      </div>
    </div>

    <div class="cart-mask" v-if="showCart" @click="showCart=false"></div>
    <div class="cart-panel" v-if="showCart">
      <h3>🛒 购物车</h3>
      <div v-for="(qty, id) in cart" :key="id" class="cart-item">
        {{ getName(id) }} × {{ qty }}
      </div>
      <div class="total">合计：¥{{ totalPrice }}</div>
      <button class="submit" @click="submitOrder">结算下单</button>
    </div>

    <div class="order-mask" v-if="showOrder" @click="showOrder=false"></div>
    <div class="order-panel" v-if="showOrder">
      <h3>📋 我的订单</h3>
      <div class="order-item" v-for="o in myOrder" :key="o.id">
        <div>订单号：{{ o.id }}</div>
        <div>订单状态：{{ o.status }}</div>
        <div>订单备注：{{ o.remark || '无' }}</div>
      </div>
      <div v-if="myOrder.length === 0" class="empty-tip">暂无订单</div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return {
      products: [],
      myOrder: [],
      cart: {},
      remark: {},
      showCart: false,
      showOrder: false,
      active: ''
    }
  },
  computed: {
    totalItems() {
      return Object.values(this.cart).reduce((a, b) => a + b, 0)
    },
    totalPrice() {
      return Object.entries(this.cart).reduce((sum, [id, qty]) => {
        const p = this.products.find(x => x.id == id)
        return sum + (p ? p.price * qty : 0)
      }, 0).toFixed(2)
    }
  },
  methods: {
    getSmartEmoji(name, id) {
      const n = name.toLowerCase()

      // 水果类
      if (n.includes('苹果')) return '🍎'
      if (n.includes('香蕉')) return '🍌'
      if (n.includes('橙')) return '🍊'
      if (n.includes('葡萄')) return '🍇'
      if (n.includes('西瓜')) return '🍉'
      if (n.includes('芒果')) return '🥭'
      if (n.includes('莓')) return '🍓'
      if (n.includes('桃')) return '🍑'
      if (n.includes('梨')) return '🍐'

      // 零食类
      if (n.includes('面包')) return '🍞'
      if (n.includes('饼')) return '🍪'
      if (n.includes('薯片')) return '🥔'
      if (n.includes('糖')) return '🍬'
      if (n.includes('巧克')) return '🍫'
      if (n.includes('肠')) return '🌭'
      if (n.includes('面')) return '🍜'
      if (n.includes('米')) return '🍚'

      // 饮品类
      if (n.includes('可乐')) return '🥤'
      if (n.includes('水')) return '💧'
      if (n.includes('奶')) return '🥛'
      if (n.includes('茶')) return '🧋'
      if (n.includes('咖啡')) return '☕'

      // 文具类
      if (n.includes('笔')) return '✏️'
      if (n.includes('本')) return '📒'
      if (n.includes('书')) return '📚'
      if (n.includes('包')) return '🎒'

      // 五金工具类
      if (n.includes('螺丝')) return '🔩'
      if (n.includes('扳')) return '🔧'
      if (n.includes('锤')) return '🔨'
      if (n.includes('电')) return '🔌'

      // 日化日用品类（重点修改）
      if (n.includes('卫生纸') || n.includes('卷纸') || n.includes('厕纸')) return '🧻'
      if (n.includes('纸巾') || n.includes('抽纸') || n.includes('面巾纸')) return '🧻'
      if (n.includes('洗衣液') || n.includes('洗护')) return '🧴'
      if (n.includes('香皂') || n.includes('肥皂') || n.includes('洗手液')) return '🧼'
      if (n.includes('洗发水') || n.includes('沐浴露')) return '🧴'
      if (n.includes('牙膏') || n.includes('牙刷')) return '🪥'
      if (n.includes('毛巾')) return '🧽'
      if (n.includes('洗洁精') || n.includes('清洁剂')) return '🧼'
      if (n.includes('口罩')) return '😷'
      if (n.includes('垃圾袋')) return '🗑️'

      // 无匹配商品 按ID分配不同表情
      const emojis = [
        '🎁', '🛍️', '📦', '🧩', '🎨', '🧸', '⚡', '🔥',
        '💎', '🌞', '🌈', '☁', '🌷', '🚀', '🚗', '🚲',
        '🛵', '🎮', '🎯', '🎲'
      ]
      const index = parseInt(id) % emojis.length
      return emojis[index]
    },

    async getAll() {
      const { data } = await axios.get('http://localhost:8080/order/product/list')
      this.products = data
      this.active = ''
    },
    async getCat(c) {
      const { data } = await axios.get('http://localhost:8080/order/product/category?category=' + c)
      this.products = data
      this.active = c
    },
    async getMyOrder() {
      const { data } = await axios.get('http://localhost:8080/order/user/order')
      this.myOrder = data
    },
    add(id, max) {
      const n = this.cart[id] || 0
      if (n < max) this.cart[id] = n + 1
    },
    sub(id) {
      const n = this.cart[id] || 0
      if (n > 1) this.cart[id]--
      else delete this.cart[id]
    },
    getName(id) {
      return this.products.find(x => x.id == id)?.name
    },
    openCart() {
      this.showCart = true
    },
    openOrder() {
      this.showOrder = true
    },
    async submitOrder() {
      const list = Object.entries(this.cart).map(([pid, qty]) => ({
        productId: +pid,
        quantity: qty,
        remark: this.remark[pid] || ''
      }))
      await axios.post('http://localhost:8080/order/batchAdd', list)
      alert('下单成功')
      this.cart = {}
      this.remark = {}
      this.showCart = false
      this.getAll()
      this.getMyOrder()
    }
  },
  mounted() {
    this.getAll();
    this.getMyOrder()
  }
}
</script>

<style scoped>
.buyer-container {
  background: #f7f8fa;
  min-height: 100vh;
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header h1 {
  font-size: 24px;
  color: #333;
}
.header-buttons {
  display: flex;
  gap: 12px;
}
.cart-button, .order-button {
  background: #409eff;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 15px;
  position: relative;
  cursor: pointer;
}
.badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: red;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.category-box {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.cat-btn {
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 10px;
  background: white;
  cursor: pointer;
}
.cat-btn.active {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.product-scroll {
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 8px;
}
.product-scroll::-webkit-scrollbar {
  width: 6px;
}
.product-scroll::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.product-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.emoji {
  font-size: 40px;
  margin-bottom: 10px;
}
.name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 6px;
}
.price {
  color: #f56c6c;
  font-size: 16px;
  margin-bottom: 6px;
}
.stock {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}
.stock.low {
  color: red;
  font-weight: bold;
}
.ctrl {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
}
.ctrl button {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #f5f5f5;
  cursor: pointer;
}
.input {
  width: 100%;
  padding: 8px;
  border: 1px solid #eee;
  border-radius: 8px;
  font-size: 13px;
}

.cart-mask, .order-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 99;
}
.cart-panel, .order-panel {
  position: fixed;
  right: 0;
  top: 0;
  width: 360px;
  height: 100vh;
  background: white;
  z-index: 100;
  padding: 30px 20px;
  overflow-y: auto;
}
.cart-panel h3, .order-panel h3 {
  margin-bottom: 20px;
  font-size: 20px;
}
.cart-item {
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}
.order-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.order-item div {
  margin-bottom: 4px;
}
.total {
  margin-top: 20px;
  font-size: 18px;
  font-weight: bold;
  text-align: right;
}
.submit {
  width: 100%;
  background: #409eff;
  color: white;
  padding: 12px;
  border: none;
  border-radius: 10px;
  margin-top: 20px;
  cursor: pointer;
}
.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #999;
}
</style>