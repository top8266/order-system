<template>
  <div class="container">
    <h2 class="page-title">在线点餐商城</h2>

    <!-- 返回商家列表按钮 -->
    <div v-if="selectedSeller" class="back-button">
      <button @click="backToSellers" class="back-btn">
        ← 返回商家列表
      </button>
      <span class="current-seller">当前店铺：{{ selectedSeller.shopName || selectedSeller.username }}</span>
    </div>

    <!-- 商家列表视图 -->
    <div v-if="!selectedSeller" class="seller-container">
      <h3 class="section-title">🏪 选择商家</h3>
      <div class="seller-grid">
        <div 
          v-for="seller in sellers" 
          :key="seller.id" 
          class="seller-card"
          @click="enterSeller(seller)"
        >
          <div class="seller-icon">{{ getSellerEmoji(seller.shopName) }}</div>
          <div class="seller-info">
            <span class="seller-name">{{ seller.shopName || seller.username }}</span>
            <span class="seller-status" :class="seller.auditStatus === 1 ? 'status-active' : 'status-pending'">
              {{ seller.auditStatus === 1 ? '营业中' : '待审核' }}
            </span>
            <span class="seller-product-count">{{ getSellerProductCount(seller.id) }} 件商品</span>
          </div>
          <div class="enter-btn">进入店铺 →</div>
        </div>
      </div>
    </div>

    <!-- 商品列表视图 -->
    <div v-else>
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
      <div class="product-container">
        <div class="product-list">
          <div class="product-card" v-for="product in products" :key="product.id">
            <!-- 核心：自动匹配Emoji -->
            <div class="product-icon">
              {{ getProductEmoji(product.name) }}
            </div>
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="price" translate="no">¥{{ product.price }}元</p>
            <p class="stock" :class="{'low':product.stock < 5}">库存：{{ product.stock }}</p>

            <div class="num-control">
              <button @click="dec(product.id)">-</button>
              <span>{{ cart[product.id] || 0 }}</span>
              <button @click="add(product.id, product.stock)">+</button>
            </div>

            <input v-model="remarkList[product.id]" placeholder="下单备注（选填）" class="remark-input" />
          </div>
        </div>
        <div v-if="products.length === 0" class="empty-state">
          <div class="empty-icon">📦</div>
          <p>该店铺暂无商品</p>
        </div>
      </div>
    </div>

    <!-- 操作按钮区域 -->
    <div class="action-buttons">
      <button class="cart-btn" @click="showCartDrawer = true">
        🛒 购物车
        <span v-if="totalItems > 0" class="badge">{{ totalItems }}</span>
      </button>
      <button class="order-btn" @click="showOrderDrawer = true">
        📋 历史订单
        <span v-if="myOrders.length > 0" class="badge">{{ Object.keys(groupedOrders).length }}</span>
      </button>
    </div>

    <!-- 购物车遮罩 -->
    <div class="overlay" v-if="showCartDrawer || showOrderDrawer" @click="showCartDrawer=false; showOrderDrawer=false"></div>
    
    <!-- 购物车侧边抽屉 -->
    <div class="drawer" :class="{open:showCartDrawer}">
      <div class="drawer-head">
        <h3>🛒 我的购物车</h3>
        <button @click="showCartDrawer=false" class="close-btn">×</button>
      </div>
      <div class="drawer-body">
        <div v-if="totalItems === 0" class="cart-empty">购物车空空如也～</div>
        <div v-else>
          <!-- 地址选择 -->
          <div class="address-select">
            <h4>📍 收货地址</h4>
            <div v-if="selectedAddress" class="selected-address">
              <div>{{ selectedAddress.name }} {{ selectedAddress.phone }}</div>
              <div class="address-text">{{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detail }}</div>
              <button class="change-btn" @click="showAddressDialog=true">更换</button>
            </div>
            <div v-else class="no-address-tip">
              <span>请选择收货地址</span>
              <button class="select-btn" @click="showAddressDialog=true">选择地址</button>
            </div>
          </div>

          <div class="cart-actions">
            <button class="clear-cart-btn" @click="clearCart">清空购物车</button>
          </div>
          <div v-for="(qty, pid) in cart" :key="pid" class="cart-item-box">
            <div class="cart-item-content">
              <div class="cart-item-header">
                <span class="cart-name">{{ getName(pid) }}</span>
                <button class="delete-item-btn" @click="deleteFromCart(pid)">删除</button>
              </div>
              <div class="cart-item-details">
                <div class="quantity-control">
                  <button @click="decreaseCart(pid)" class="qty-btn">-</button>
                  <input 
                    type="number" 
                    :value="qty" 
                    @change="updateCartQuantity(pid, $event.target.value)"
                    class="qty-input"
                    min="1"
                    :max="getProductStock(pid)"
                  />
                  <button @click="increaseCart(pid)" class="qty-btn">+</button>
                </div>
                <span class="cart-price">¥{{ getPrice(pid)*qty }}</span>
              </div>
              <div v-if="remarkList[pid]" class="cart-remark">备注：{{ remarkList[pid] }}</div>
            </div>
          </div>
          <div class="cart-total">合计：¥{{ totalPrice.toFixed(2) }}</div>
          <button class="submit-btn" @click="submit()">立即结算下单</button>
        </div>
      </div>
    </div>

    <!-- 历史订单侧边抽屉 -->
    <div class="drawer order-drawer" :class="{open:showOrderDrawer}">
      <div class="drawer-head">
        <h3>📋 历史订单</h3>
        <button @click="showOrderDrawer=false" class="close-btn">×</button>
      </div>
      <div class="drawer-body">
        <div v-if="myOrders.length === 0" class="cart-empty">暂无下单记录</div>
        <div v-else>
          <div v-for="(group, orderNo) in groupedOrders" :key="orderNo" class="order-group">
            <div class="order-header">
              <span>订单编号：{{ orderNo }}</span>
              <span class="order-status" :class="getStatusClass(group[0].status)">{{ group[0].status }}</span>
              <button class="delete-btn" @click="deleteOrder(orderNo, group)">🗑️ 删除</button>
            </div>
            <!-- 订单进度条 -->
            <div class="order-progress">
              <div class="progress-step" :class="{ active: getProgressStep(group[0].status) >= 1 }">
                <span class="step-icon">📝</span>
                <span class="step-text">下单</span>
              </div>
              <div class="progress-step" :class="{ active: getProgressStep(group[0].status) >= 2 }">
                <span class="step-icon">✅</span>
                <span class="step-text">接单</span>
              </div>
              <div class="progress-step" :class="{ active: getProgressStep(group[0].status) >= 3 }">
                <span class="step-icon">📦</span>
                <span class="step-text">备货</span>
              </div>
              <div class="progress-step" :class="{ active: getProgressStep(group[0].status) >= 4 }">
                <span class="step-icon">🚚</span>
                <span class="step-text">配送</span>
              </div>
              <div class="progress-step" :class="{ active: getProgressStep(group[0].status) >= 5 }">
                <span class="step-icon">🎉</span>
                <span class="step-text">完成</span>
              </div>
            </div>
            <div class="order-products">
              <div v-for="item in group" :key="item.id" class="order-product-item">
                <span>{{ item.productName || getProductName(item.productId) }} ×{{ item.quantity || 1 }}</span>
                <span v-if="item.remark" class="remark">备注：{{ item.remark }}</span>
              </div>
            </div>
            <div class="order-info-extra">
              <div v-if="group[0].address" class="info-row">
                <span class="info-label">📍 地址：</span>
                <span class="info-value">{{ group[0].address }}</span>
              </div>
              <!-- 备货阶段显示商家信息 -->
              <div v-if="['备货', '备货中', '待配送'].includes(group[0].status) && group[0].sellerPhone" class="info-row">
                <span class="info-label">🏪 商家电话：</span>
                <span class="info-value phone">{{ group[0].sellerPhone }}</span>
              </div>
              <!-- 配送阶段显示骑手信息 -->
              <div v-if="group[0].status === '配送中'" class="info-row">
                <span class="info-label">🚴 骑手：</span>
                <span class="info-value phone">{{ group[0].riderName || '骑手' }} {{ group[0].riderPhone || '' }}</span>
              </div>
              <!-- 其他状态显示商家电话 -->
              <div v-if="!['备货', '备货中', '待配送', '配送中'].includes(group[0].status) && group[0].sellerPhone" class="info-row">
                <span class="info-label">📞 商家电话：</span>
                <span class="info-value phone">{{ group[0].sellerPhone }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址选择弹窗 -->
    <div class="dialog-mask" v-if="showAddressDialog" @click="showAddressDialog=false"></div>
    <div class="address-dialog" v-if="showAddressDialog">
      <div class="dialog-head">
        <h3>选择收货地址</h3>
        <button @click="showAddressDialog=false" class="close-btn">×</button>
      </div>
      <div class="dialog-body">
        <div v-for="addr in addresses" :key="addr.id" class="address-option" :class="{ selected: selectedAddress?.id === addr.id }" @click="selectAddress(addr)">
          <div class="addr-name">{{ addr.name }} {{ addr.phone }}</div>
          <div class="addr-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
          <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
        </div>
        <div v-if="addresses.length === 0" class="no-address">
          暂无地址，请先添加
          <router-link to="/address" class="add-link">去添加</router-link>
        </div>
        <div class="dialog-footer">
          <router-link to="/address" class="manage-address-btn" @click="showAddressDialog=false">
            + 添加/管理地址
          </router-link>
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
      sellers: [],
      myOrders: [],
      cart: {},
      remarkList: {},
      showCartDrawer: false,
      showOrderDrawer: false,
      activeCat: '',
      selectedSeller: null,
      addresses: [],
      selectedAddress: null,
      showAddressDialog: false
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
    },
    groupedOrders() {
      const groups = {}
      this.myOrders.forEach(order => {
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
    getToken() {
      return localStorage.getItem('token')
    },
    
    // 获取订单进度步骤
    getProgressStep(status) {
      switch(status) {
        case '待接单': return 1
        case '已接单': return 2
        case '备货中': return 3
        case '配送中': return 4
        case '已送达': return 5
        case '已完成': return 5
        default: return 1
      }
    },
    
    // 获取状态样式类
    getStatusClass(status) {
      switch(status) {
        case '待接单': return 'status-pending'
        case '已接单': return 'status-accepted'
        case '备货中': return 'status-preparing'
        case '配送中': return 'status-delivering'
        case '已送达': return 'status-done'
        case '已完成': return 'status-done'
        default: return ''
      }
    },

    // 根据店铺名称获取对应的图标
    getSellerEmoji(shopName) {
      if (!shopName) return '🏬'
      
      // 奶茶店/饮品店
      if(shopName.includes('蜜雪') || shopName.includes('益禾堂') || shopName.includes('喜茶')) return '🧋'
      
      // 川菜/湘菜馆
      if(shopName.includes('川湘') || shopName.includes('川菜') || shopName.includes('湘菜')) return '🍲'
      
      // 水果店/鲜果店
      if(shopName.includes('鲜果') || shopName.includes('水果')) return '🍇'
      
      // 沙县小吃
      if(shopName.includes('沙县')) return '🥟'
      
      // 汉堡店/西式快餐
      if(shopName.includes('汉堡') || shopName.includes('炸鸡')) return '🍔'
      
      // 兰州拉面/面馆
      if(shopName.includes('兰州') || shopName.includes('拉面') || shopName.includes('面馆')) return '🍜'
      
      // 黄焖鸡
      if(shopName.includes('黄焖鸡')) return '🍗'
      
      // 鸡排店
      if(shopName.includes('鸡排') || shopName.includes('正新')) return '🍟'
      
      // 便利店
      if(shopName.includes('便利') || shopName.includes('超市')) return '🏪'
      
      // 默认图标
      return '🏬'
    },

    // 关键方法：自动根据商品名匹配Emoji
    getProductEmoji(name){
      // 水果类
      if(name.includes('苹果')) return '🍎'
      if(name.includes('香蕉') || name.includes('香蕉奶昔')) return '🍌'
      if(name.includes('橙子') || name.includes('橘子') || name.includes('橙') || name.includes('橙汁') || name.includes('橙橙')) return '🍊'
      if(name.includes('葡萄') || name.includes('葡萄汁')) return '🍇'
      if(name.includes('西瓜')) return '🍉'
      if(name.includes('芒果') || name.includes('芒芒')) return '🥭'
      if(name.includes('草莓') || name.includes('莓莓')) return '🍓'
      if(name.includes('柠檬')) return '🍋'
      if(name.includes('红柚')) return '🍊'
      if(name.includes('蓝莓')) return '🫐'
      if(name.includes('百香果')) return '🥭'
      if(name.includes('黑提')) return '🍇'

      // 饮品类
      if(name.includes('可乐')) return '🥤'
      if(name.includes('牛奶') || name.includes('奶昔')) return '🥛'
      if(name.includes('矿泉水')) return '💧'
      if(name.includes('奶茶') || name.includes('奶盖') || name.includes('芋圆') || name.includes('芋泥') || name.includes('啵啵') || name.includes('四季奶青') || name.includes('波波') || name.includes('烤奶') || name.includes('珍珠奶茶') || name.includes('红豆奶茶') || name.includes('椰椰奶茶') || name.includes('黑糖珍珠') || name.includes('黑糖虎纹') || name.includes('抹茶奶绿') || name.includes('椰椰奶冻')) return '🧋'
      if(name.includes('酸奶')) return '🥛'
      if(name.includes('果汁') || name.includes('芒果汁')) return '🧃'
      if(name.includes('冰沙') || name.includes('冰')) return '🧊'
      if(name.includes('酸梅汤')) return '🍹'
      if(name.includes('杨枝甘露')) return '🥭'
      if(name.includes('芝芝莓莓')) return '🍓'
      if(name.includes('多肉葡萄')) return '🍇'
      if(name.includes('蜜桃四季春')) return '🍑'
      if(name.includes('纯绿研茶')) return '🍵'
      if(name.includes('四季春')) return '🌸'

      // 零食类
      if(name.includes('面包')) return '🍞'
      if(name.includes('薯片')) return '🥔'
      if(name.includes('饼干')) return '🍪'
      if(name.includes('火腿肠')) return '🌭'
      if(name.includes('方便面') || name.includes('泡面')) return '🍜'
      if(name.includes('薯条')) return '🍟'
      if(name.includes('冰淇淋') || name.includes('甜筒') || name.includes('圣代')) return '🍦'
      if(name.includes('烤布蕾')) return '🍮'
      if(name.includes('鸡排') || name.includes('鸡柳') || name.includes('鸡米花')) return '🍗'
      if(name.includes('香肠')) return '🌭'
      if(name.includes('鸡翅') || name.includes('鸡腿')) return '🍗'
      if(name.includes('骨肉相连')) return '🍖'
      if(name.includes('洋葱圈')) return '🧅'
      if(name.includes('鸡块')) return '🍗'

      // 主食类
      if(name.includes('面条') || name.includes('拉面') || name.includes('炒面') || name.includes('刀削面') || name.includes('葱油拌面')) return '🍝'
      if(name.includes('饺子') || name.includes('蒸饺')) return '🥟'
      if(name.includes('米饭') || name.includes('炒饭') || name.includes('卤肉饭')) return '🍚'
      if(name.includes('包子')) return '🥟'
      if(name.includes('汉堡') || name.includes('牛肉堡') || name.includes('鸡腿堡')) return '🍔'
      if(name.includes('麻辣香锅')) return '🍲'
      if(name.includes('水煮鱼') || name.includes('酸菜鱼')) return '🐟'
      if(name.includes('回锅肉')) return '🥓'
      if(name.includes('宫保鸡丁') || name.includes('辣子鸡') || name.includes('口水鸡')) return '🍗'
      if(name.includes('麻婆豆腐') || name.includes('黄焖豆腐') || name.includes('豆腐')) return '🥘'
      if(name.includes('拌面') || name.includes('拌米粉') || name.includes('炒米粉')) return '🍜'
      if(name.includes('馄饨')) return '🍲'
      if(name.includes('鸡腿饭')) return '🍗'
      if(name.includes('老鸭汤')) return '🦆'
      if(name.includes('炸鸡')) return '🍗'
      if(name.includes('牛肉面')) return '🍜'
      if(name.includes('凉拌牛肉')) return '🥩'
      if(name.includes('鸡蛋汤') || name.includes('紫菜蛋花汤') || name.includes('紫菜汤')) return '🥚'
      if(name.includes('黄焖鸡')) return '🍗'
      if(name.includes('黄焖排骨') || name.includes('黄焖牛肉') || name.includes('黄焖羊肉')) return '🍖'
      if(name.includes('黄焖茄子')) return '🍆'
      if(name.includes('水果拼盘') || name.includes('水果沙拉')) return '🥗'
      if(name.includes('毛血旺')) return '🍲'
      if(name.includes('鱼香肉丝')) return '🥢'
      if(name.includes('羊肉泡馍')) return '🥣'
      if(name.includes('牛肉汤')) return '🥘'

      // 凉菜/小菜类
      if(name.includes('凉拌黄瓜')) return '🥒'
      if(name.includes('凉拌木耳')) return '🍄'
      if(name.includes('卤蛋')) return '🥚'

      // 五金工具类
      if(name.includes('螺丝') || name.includes('螺母')) return '🔩'
      if(name.includes('扳手') || name.includes('钳子')) return '🔧'
      if(name.includes('锤子')) return '🔨'

      // 文具类
      if(name.includes('笔记本') || name.includes('本子')) return '📒'
      if(name.includes('钢笔') || name.includes('笔')) return '🖋️'
      if(name.includes('书包')) return '🎒'

      // 日化类
      if(name.includes('洗衣液') || name.includes('洗涤')) return '🧴'
      if(name.includes('洗洁精')) return '🧽'
      if(name.includes('卫生纸') || name.includes('纸巾')) return '🧻'
      if(name.includes('牙膏')) return '🦷'
      if(name.includes('洗发水')) return '🧴'
      if(name.includes('沐浴露')) return '🛁'
      if(name.includes('肥皂') || name.includes('香皂')) return '🧼'
      if(name.includes('毛巾')) return '🧣'
      if(name.includes('牙刷')) return '🪥'

      // 默认图标
      return '📦'
    },

    async getProducts() {
      if (this.selectedSeller) {
        // 获取当前商家的商品（包括未分配的商品）
        const {data} = await axios.get(`http://localhost:8080/order/product/seller/${this.selectedSeller.id}`)
        this.products = data
      } else {
        // 获取所有商品（用于统计商家商品数量）
        const {data} = await axios.get('http://localhost:8080/order/product/list')
        this.products = data
      }
      this.activeCat = ''
    },
    async getByCategory(cat) {
      let url = 'http://localhost:8080/order/product/category?category='+cat
      if (this.selectedSeller) {
        url += '&sellerId=' + this.selectedSeller.id
      }
      const {data} = await axios.get(url)
      this.products = data
      this.activeCat = cat
    },
    async getMyOrders() {
      const {data} = await axios.get('http://localhost:8080/order/user/order')
      this.myOrders = data
    },
    async deleteOrder(orderNo, group) {
      if (!confirm(`确定要删除订单 ${orderNo} 吗？`)) {
        return
      }
      const ids = group.map(item => item.id)
      try {
        await axios.post('http://localhost:8080/order/batchDelete', ids)
        alert('删除成功！')
        await this.getMyOrders()
      } catch (error) {
        alert('删除失败：' + error.message)
      }
    },
    async loadAddresses() {
      const token = this.getToken()
      if (!token) return
      try {
        const { data } = await axios.get('http://localhost:8080/address/list', {
          headers: { Authorization: token }
        })
        if (data.success) {
          this.addresses = data.data
          // 自动选择默认地址
          const defaultAddr = this.addresses.find(a => a.isDefault === 1)
          if (defaultAddr) {
            this.selectedAddress = defaultAddr
          }
        }
      } catch (error) {
        console.error('获取地址失败', error)
      }
    },
    selectAddress(addr) {
      this.selectedAddress = addr
      this.showAddressDialog = false
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
    deleteFromCart(pid) {
      if(confirm('确定要从购物车删除该商品？')) {
        delete this.cart[pid]
        delete this.remarkList[pid]
      }
    },
    clearCart() {
      if(confirm('确定要清空购物车？')) {
        this.cart = {}
        this.remarkList = {}
      }
    },
    increaseCart(pid) {
      const maxStock = this.getProductStock(pid)
      const now = this.cart[pid] || 0
      if (now < maxStock) {
        this.cart[pid] = now + 1
      } else {
        alert('已达到最大库存限制')
      }
    },
    decreaseCart(pid) {
      const now = this.cart[pid] || 0
      if (now > 1) {
        this.cart[pid] = now - 1
      } else if (now === 1) {
        if(confirm('确定要删除该商品？')) {
          delete this.cart[pid]
          delete this.remarkList[pid]
        }
      }
    },
    updateCartQuantity(pid, value) {
      const maxStock = this.getProductStock(pid)
      let newQty = parseInt(value)
      if (isNaN(newQty) || newQty < 1) {
        newQty = 1
      }
      if (newQty > maxStock) {
        alert(`最多只能购买${maxStock}件`)
        newQty = maxStock
      }
      this.cart[pid] = newQty
    },
    getProductStock(pid) {
      const product = this.products.find(x => x.id == pid)
      return product ? product.stock : 0
    },
    getName(pid) {
      return this.products.find(x=>x.id==pid)?.name || ''
    },
    getPrice(pid) {
      return this.products.find(x=>x.id==pid)?.price || 0
    },
    getProductName(pid) {
      return this.products.find(x=>x.id==pid)?.name || '未知商品'
    },
    async submit() {
      if (!this.selectedAddress) {
        alert('请先选择收货地址')
        return
      }
      
      const list = Object.entries(this.cart).map(([pid,qty])=>({
        productId: parseInt(pid),
        quantity: qty,
        remark: this.remarkList[pid]||''
      }))

      // 构建完整地址字符串
      const addr = this.selectedAddress
      const fullAddress = `${addr.province || ''}${addr.city || ''}${addr.district || ''}${addr.detail || ''}`.trim()

      // 添加地址信息
      const orderData = {
        cartList: list,
        address: fullAddress,
        userName: addr.name || '',
        userPhone: addr.phone || ''
      }

      await axios.post('http://localhost:8080/order/batchAdd', orderData)
      alert('🎉 下单成功！')
      this.cart = {}
      this.remarkList = {}
      this.showCartDrawer = false
      this.getProducts()
      this.getMyOrders()
    },
    
    // 获取商家列表
    async getSellers() {
      const {data} = await axios.get('http://localhost:8080/admin/sellers')
      this.sellers = data
    },
    
    // 进入商家店铺
    async enterSeller(seller) {
      this.selectedSeller = seller
      this.activeCat = ''
      await this.getProducts()
    },
    
    // 返回商家列表
    async backToSellers() {
      this.selectedSeller = null
      this.activeCat = ''
      await this.getProducts()
    },
    
    // 获取商家商品数量
    getSellerProductCount(sellerId) {
      return this.products.filter(p => p.sellerId === sellerId).length
    }
  },
  mounted() {
    this.getProducts()
    this.getMyOrders()
    this.getSellers()
    this.loadAddresses()
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

/* 返回按钮 */
.back-button{
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}
.back-btn{
  padding: 10px 20px;
  background: #fff;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}
.back-btn:hover{
  background: #f5f7fa;
  border-color: #409eff;
  color: #409eff;
}
.current-seller{
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

/* 商家列表 */
.seller-container{
  margin-bottom: 30px;
}
.seller-grid{
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.seller-card{
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 15px 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: box-shadow 0.3s;
}
.seller-card:hover{
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}
.seller-icon{
  font-size: 36px;
  margin-right: 12px;
}
.seller-info{
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}
.seller-name{
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.seller-status{
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}
.seller-product-count{
  font-size: 13px;
  color: #888;
}
.enter-btn{
  padding: 8px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  transition: transform 0.2s;
}
.enter-btn:hover{
  transform: scale(1.02);
}
.seller-status.status-active{
  background: #e8f5e9;
  color: #27ae60;
}
.seller-status.status-pending{
  background: #fff3e0;
  color: #f39c12;
}
.seller-product-count{
  font-size: 14px;
  color: #999;
  margin-bottom: 18px;
}
.seller-username{
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}
.enter-btn{
  display: inline-block;
  padding: 10px 25px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: #fff;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}
.seller-card:hover .enter-btn{
  transform: scale(1.05);
}

/* 空状态 */
.empty-state{
  text-align: center;
  padding: 60px 20px;
  color: #999;
}
.empty-icon{
  font-size: 64px;
  margin-bottom: 15px;
}

.action-buttons{
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 30px;
  padding-bottom: 30px;
}
.cart-btn{
  background: #409eff;
  color: #fff;
  border: none;
  padding: 14px 30px;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(64,158,255,0.3);
  transition: all 0.3s;
  min-width: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.cart-btn:hover{
  background: #337ecc;
  transform: translateY(-2px);
}
.order-btn{
  background: #67c23a;
  color: #fff;
  border: none;
  padding: 14px 30px;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(103,194,58,0.3);
  transition: all 0.3s;
  min-width: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.order-btn:hover{
  background: #529b2f;
  transform: translateY(-2px);
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

.product-container{
  max-height: 80vh;
  overflow-y: auto;
  padding-right: 10px;
  margin-bottom: 20px;
}
.product-container::-webkit-scrollbar{
  width: 6px;
}
.product-container::-webkit-scrollbar-track{
  background: #f1f1f1;
  border-radius: 3px;
}
.product-container::-webkit-scrollbar-thumb{
  background: #c1c1c1;
  border-radius: 3px;
}
.product-container::-webkit-scrollbar-thumb:hover{
  background: #a8a8a8;
}
.product-list{
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  justify-content: center;
  padding-bottom: 20px;
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
.product-seller{
  font-size: 12px;
  color: #999;
  background: #f5f7fa;
  padding: 3px 8px;
  border-radius: 10px;
  display: inline-block;
  margin-bottom: 8px;
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
.order-group{
  margin-bottom: 20px;
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
}
.order-header{
  display: flex;
  justify-content: space-between;
  padding: 14px 16px;
  background: #f8f9fa;
  color: #555;
  font-size: 14px;
}
.order-products{
  padding: 12px 16px;
}
.order-product-item{
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
  display: flex;
  align-items: center;
  gap: 12px;
}
.order-product-item:last-child{
  border-bottom: none;
}
.order-product-item .remark{
  font-size: 13px;
  color: #888;
}
.order-info-extra{
  padding: 10px 16px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}
.order-info-extra .info-row{
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 13px;
}
.order-info-extra .info-row:last-child{
  margin-bottom: 0;
}
.order-info-extra .info-label{
  color: #666;
  white-space: nowrap;
}
.order-info-extra .info-value{
  color: #333;
}
.order-info-extra .info-value.phone{
  color: #409eff;
  font-weight: 500;
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
.cart-actions{
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-end;
}
.clear-cart-btn{
  background: #f56c6c;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}
.clear-cart-btn:hover{
  background: #f23c3c;
}
.cart-item-box{
  background: #f8f9fa;
  border: 1px solid #e5e6eb;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s;
}
.cart-item-box:hover{
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  border-color: #dcdfe6;
}
.cart-item-content{
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cart-item-header{
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cart-name{
  font-weight: 600;
  font-size: 15px;
  color: #333;
}
.delete-item-btn{
  background: none;
  border: none;
  color: #f56c6c;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}
.delete-item-btn:hover{
  background: #fee;
}
.cart-item-details{
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
}
.quantity-control{
  display: flex;
  align-items: center;
  gap: 8px;
}
.qty-btn{
  width: 28px;
  height: 28px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f5f7fa;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qty-btn:hover{
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}
.qty-input{
  width: 50px;
  height: 28px;
  text-align: center;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border 0.3s;
}
.qty-input:focus{
  border-color: #409eff;
}
.cart-quantity{
  color: #909399;
}
.cart-price{
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
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

/* 地址选择样式 */
.address-select {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 20px;
}
.address-select h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
}
.selected-address {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.selected-address div:first-child {
  font-weight: 600;
  color: #333;
}
.address-text {
  color: #666;
  font-size: 13px;
}
.change-btn {
  background: #409eff;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  margin-top: 6px;
}
.no-address-tip {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.select-btn {
  background: #409eff;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

/* 地址选择弹窗 */
.address-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 400px;
  background: #fff;
  border-radius: 16px;
  z-index: 1001;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.address-dialog .dialog-head {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e6eb;
}
.address-dialog .dialog-head h3 {
  margin: 0;
  color: #333;
}
.address-dialog .dialog-body {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
}
.address-option {
  padding: 15px;
  border: 2px solid #e5e6eb;
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}
.address-option:hover {
  border-color: #409eff;
  background: #ecf5ff;
}
.address-option.selected {
  border-color: #409eff;
  background: #ecf5ff;
}
.addr-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}
.addr-detail {
  color: #666;
  font-size: 13px;
}
.default-tag {
  background: #409eff;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: inline-block;
  margin-top: 8px;
}
.no-address {
  text-align: center;
  color: #999;
  padding: 40px;
}
.add-link {
  color: #409eff;
  text-decoration: none;
  margin-left: 8px;
}

.dialog-footer {
  padding: 15px 20px;
  border-top: 1px solid #e5e6eb;
  text-align: center;
}
.manage-address-btn {
  display: inline-block;
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  color: #fff;
  text-decoration: none;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
}
.manage-address-btn:hover {
  opacity: 0.9;
}

/* 订单进度条 */
.order-progress {
  display: flex;
  justify-content: space-between;
  padding: 15px 10px;
  background: #f8f9fa;
  border-radius: 8px;
  margin: 10px 0;
}
.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  opacity: 0.4;
  transition: all 0.3s;
}
.progress-step.active {
  opacity: 1;
}
.step-icon {
  font-size: 20px;
}
.step-text {
  font-size: 12px;
  color: #666;
}
.progress-step.active .step-text {
  color: #409eff;
  font-weight: 600;
}

/* 订单状态样式 */
.order-status {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
}
.status-pending { background: #fef0f0; color: #f56c6c; }
.status-accepted { background: #fdf6ec; color: #e6a23c; }
.status-preparing { background: #f4f4f5; color: #909399; }
.status-delivering { background: #ecf5ff; color: #409eff; }
.status-done { background: #f0f9eb; color: #67c23a; }
.delete-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  background: #fff;
  color: #f56c6c;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.delete-btn:hover {
  background: #fef0f0;
}
</style>