<template>
  <div class="buyer-page">
    <div class="page-header">
      <h1>订单管理系统</h1>
      <p>用户下单页面</p>
    </div>

    <div class="container">
      <h2 class="title">商品列表</h2>
      <div class="product-list">
        <div v-for="item in products" :key="item.id" class="product-item">
          <div class="info">
            <div class="name">{{ item.name }}</div>
            <div class="price">¥{{ item.price }}</div>
          </div>
          <button class="order-btn" @click="submitOrder(item.id)">立即下单</button>
        </div>
      </div>
    </div>

    <div class="container">
      <h2 class="title">我的订单</h2>
      <div class="order-list">
        <div v-for="o in orders" :key="o.id" class="order-item">
          <span>订单编号：{{ o.id }}</span>
          <span class="status">{{ o.status }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const products = ref([])
const orders = ref([])

onMounted(() => {
  axios.get('http://localhost:8080/order/product/list').then(res => {
    products.value = res.data
  })
  axios.get('http://localhost:8080/order/list').then(res => {
    orders.value = res.data
  })
})

const submitOrder = (productId) => {
  axios.post('http://localhost:8080/order/add', {
    userId: 1,
    productId: productId
  }).then(res => {
    alert('下单成功')
    axios.get('http://localhost:8080/order/list').then(res => {
      orders.value = res.data
    })
  })
}
</script>

<style scoped>
.buyer-page {
  background: #ffffff;
  color: #333;
  min-height: 100vh;
  padding: 0;
  margin: 0;
  font-family: "Microsoft YaHei", sans-serif;
}

.page-header {
  background: #222;
  color: white;
  padding: 20px 40px;
  text-align: center;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

.page-header p {
  margin: 5px 0 0;
  opacity: 0.8;
}

.container {
  max-width: 1000px;
  margin: 30px auto;
  padding: 0 20px;
}

.title {
  font-size: 18px;
  border-left: 4px solid #222;
  padding-left: 10px;
  margin-bottom: 15px;
}

.product-list {
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}

.product-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.product-item:last-child {
  border-bottom: none;
}

.info .name {
  font-size: 16px;
  font-weight: 500;
}

.info .price {
  color: #666;
  margin-top: 4px;
}

.order-btn {
  background: #111;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.order-btn:hover {
  background: #333;
}

.order-list {
  border: 1px solid #eee;
  border-radius: 6px;
}

.order-item {
  padding: 12px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
}

.order-item:last-child {
  border-bottom: none;
}

.status {
  color: #000;
  font-weight: bold;
}
</style>