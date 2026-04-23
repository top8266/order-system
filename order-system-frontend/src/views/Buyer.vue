<template>
  <div class="container">
    <h2 style="text-align: center; margin-bottom: 30px;">商品列表</h2>

    <!-- 购物车按钮 -->
    <button class="cart-btn" @click="showCartDrawer = true">
      🛒 购物车
      <span v-if="Object.keys(cart).length > 0" class="badge">{{ totalItems }}</span>
    </button>

    <!-- 商品列表 -->
    <div class="product-list">
      <div class="product-card" v-for="product in products" :key="product.id">
        <div class="product-icon">
          {{
            {
              '矿泉水': '💧',
              '面包': '🍞',
              '方便面': '🍜',
              '可乐': '🥤',
              '薯片': '🥔',
              '火腿肠': '🌭',
              '牛奶': '🥛',
              '饼干': '🍪'
            }[product.name] || '📦'
          }}
        </div>
        <h3>{{ product.name }}</h3>
        <p class="price">¥{{ product.price }}</p>
        <p class="stock" :class="{'low-stock': product.stock < 5}">库存: {{ product.stock }}</p>

        <div class="quantity-control">
          <button @click="decreaseQuantity(product.id)">-</button>
          <span>{{ cart[product.id] || 0 }}</span>
          <button @click="increaseQuantity(product.id, product.stock)">+</button>
        </div>

        <input
          type="text"
          placeholder="下单备注（选填）"
          v-model="remarkList[product.id]"
          class="remark-input"
        >
      </div>
    </div>

    <!-- 购物车抽屉 -->
    <div class="drawer-overlay" v-if="showCartDrawer" @click="showCartDrawer = false"></div>
    <div class="cart-drawer" :class="{ open: showCartDrawer }">
      <div class="drawer-header">
        <h3>我的购物车</h3>
        <button @click="showCartDrawer = false" class="close-btn">×</button>
      </div>

      <div class="drawer-body">
        <div v-if="Object.keys(cart).length === 0" class="empty-cart">购物车为空</div>
        <div v-else>
          <div class="cart-item" v-for="(quantity, productId) in cart" :key="productId">
            <div class="item-main">
              <span class="item-name">{{ getProductName(productId) }}</span>
              <span>× {{ quantity }}</span>
            </div>
            <span class="item-price">¥{{ getProductPrice(productId) * quantity }}</span>
            <div v-if="remarkList[productId]" class="item-remark">备注: {{ remarkList[productId] }}</div>
          </div>
          <div class="cart-total">
            <strong>总计: ¥{{ totalPrice }}</strong>
          </div>
          <button @click="submitCart" class="submit-btn">确认下单</button>
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
      cart: {},
      remarkList: {},
      showCartDrawer: false
    };
  },
  computed: {
    totalPrice() {
      return Object.entries(this.cart).reduce((sum, [productId, quantity]) => {
        const product = this.products.find(p => p.id === parseInt(productId));
        return sum + (product ? product.price * quantity : 0);
      }, 0);
    },
    totalItems() {
      return Object.values(this.cart).reduce((sum, qty) => sum + qty, 0);
    }
  },
  methods: {
    async getProducts() {
      const res = await axios.get("http://localhost:8080/order/product/list");
      this.products = res.data;
    },
    increaseQuantity(productId, maxStock) {
      const current = this.cart[productId] || 0;
      if (current < maxStock) {
        this.cart[productId] = current + 1;
      } else {
        alert("库存不足");
      }
    },
    decreaseQuantity(productId) {
      const current = this.cart[productId] || 0;
      if (current > 0) {
        this.cart[productId] = current - 1;
        if (this.cart[productId] === 0) {
          delete this.cart[productId];
          delete this.remarkList[productId];
        }
      }
    },
    getProductName(productId) {
      return this.products.find(p => p.id === parseInt(productId))?.name || "";
    },
    getProductPrice(productId) {
      return this.products.find(p => p.id === parseInt(productId))?.price || 0;
    },
    async submitCart() {
      const cartList = Object.entries(this.cart).map(([productId, quantity]) => ({
        productId: parseInt(productId),
        quantity,
        remark: this.remarkList[productId] || ""
      }));
      const res = await axios.post("http://localhost:8080/order/batchAdd", cartList);
      if (res.data === "success") {
        alert("下单成功！");
        this.cart = {};
        this.remarkList = {};
        this.showCartDrawer = false;
        this.getProducts();
      } else {
        alert(res.data);
      }
    }
  },
  mounted() {
    this.getProducts();
  }
};
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

.cart-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  background: #1890ff;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 16px;
  z-index: 100;
}
.badge {
  background: red;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  margin-left: 8px;
}

.product-list {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 40px;
  justify-content: center;
}
.product-card {
  border: 1px solid #eee;
  padding: 20px;
  border-radius: 12px;
  width: 200px;
  text-align: center;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.product-icon {
  font-size: 40px;
  margin-bottom: 12px;
}
.price {
  color: #ff4d4f;
  font-weight: bold;
  font-size: 18px;
}
.stock {
  font-size: 14px;
  color: #666;
}
.low-stock {
  color: red;
}
.quantity-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin: 12px 0;
}
.quantity-control button {
  width: 28px;
  height: 28px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.remark-input {
  width: 100%;
  margin-top: 8px;
  padding: 6px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 999;
}
.cart-drawer {
  position: fixed;
  top: 0;
  right: -320px;
  width: 320px;
  height: 100%;
  background: white;
  box-shadow: -2px 0 8px rgba(0,0,0,0.1);
  transition: right 0.3s ease;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}
.cart-drawer.open {
  right: 0;
}
.drawer-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.close-btn {
  border: none;
  background: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}
.drawer-body {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}
.empty-cart {
  text-align: center;
  color: #999;
  margin-top: 40px;
}
.cart-item {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #eee;
}
.item-main {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}
.item-name {
  font-weight: 500;
}
.item-price {
  color: #ff4d4f;
  font-weight: bold;
}
.item-remark {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
  padding-left: 4px;
}
.cart-total {
  text-align: right;
  margin-top: 20px;
  margin-bottom: 20px;
  font-size: 18px;
  color: #ff4d4f;
}
.submit-btn {
  width: 100%;
  background: #1890ff;
  color: white;
  border: none;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
</style>