<template>
  <div class="seller-page">
    <div class="page-header">
      <h1>订单管理系统</h1>
      <p>管理员后台</p>
    </div>

    <div class="container">
      <h2 class="title">订单管理</h2>
      <div class="order-table">
        <div v-for="o in orders" :key="o.id" class="order-row">
          <div>订单：{{ o.id }}</div>
          <div>商品：{{ o.productId }}</div>
          <div>用户：{{ o.userId }}</div>
          <div class="status">{{ o.status }}</div>
          <div class="btns">
            <button class="btn finish" @click="finish(o.id)">完成</button>
            <button class="btn delete" @click="del(o.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
const orders = ref([])

load()
function load() {
  axios.get('http://localhost:8080/order/list').then(res => {
    orders.value = res.data
  })
}

function finish(id) {
  axios.post('http://localhost:8080/order/update', {
    id, status: '已完成'
  }).then(() => load())
}

function del(id) {
  if (confirm('确认删除？')) {
    axios.delete('http://localhost:8080/order/delete/' + id).then(() => load())
  }
}
</script>

<style scoped>
.seller-page {
  background: #fff;
  color: #333;
  min-height: 100vh;
  font-family: "Microsoft YaHei", sans-serif;
}

.page-header {
  background: #222;
  color: white;
  padding: 20px;
  text-align: center;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

.container {
  max-width: 1100px;
  margin: 30px auto;
  padding: 0 20px;
}

.title {
  font-size: 18px;
  border-left: 4px solid #222;
  padding-left: 10px;
  margin-bottom: 15px;
}

.order-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border: 1px solid #eee;
  margin-bottom: 8px;
  border-radius: 4px;
}

.status {
  font-weight: bold;
}

.btns {
  display: flex;
  gap: 10px;
}

.btn {
  border: none;
  padding: 6px 12px;
  border-radius: 3px;
  cursor: pointer;
}

.finish {
  background: #333;
  color: white;
}

.delete {
  background: #555;
  color: white;
}
</style>