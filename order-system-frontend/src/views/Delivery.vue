<template>
  <div class="delivery-container">
    <h2>🚴 接单配送</h2>
    
    <!-- 待接单列表 -->
    <div class="section">
      <h3>📋 待配送订单</h3>
      <div class="order-list">
        <div v-if="pendingOrders.length === 0" class="empty">暂无待配送订单</div>
        <div v-for="order in pendingOrders" :key="order.orderNo" class="order-card pending">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status">待配送</span>
          </div>
          <div class="order-products">
            <div v-for="item in order.items" :key="item.productId" class="product-item">
              <span>{{ item.productName }}</span>
              <span>× {{ item.quantity }}</span>
            </div>
          </div>
          <div class="order-info">
            <span>📍 地址：{{ order.address || '未填写' }}</span>
          </div>
          <div class="order-actions">
            <button class="accept-btn" @click="acceptOrder(order.orderNo)">接单配送</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 配送中列表 -->
    <div class="section">
      <h3>📦 配送中</h3>
      <div class="order-list">
        <div v-if="shippingOrders.length === 0" class="empty">暂无配送中订单</div>
        <div v-for="order in shippingOrders" :key="order.orderNo" class="order-card shipping">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status">配送中</span>
          </div>
          <div class="order-products">
            <div v-for="item in order.items" :key="item.productId" class="product-item">
              <span>{{ item.productName }}</span>
              <span>× {{ item.quantity }}</span>
            </div>
          </div>
          <div class="order-info">
            <span>📍 地址：{{ order.address || '未填写' }}</span>
          </div>
          <div class="order-actions">
            <button class="complete-btn" @click="completeOrder(order.orderNo)">确认送达</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 已完成列表 -->
    <div class="section">
      <h3>✅ 已送达</h3>
      <div class="order-list">
        <div v-if="completedOrders.length === 0" class="empty">暂无已送达订单</div>
        <div v-for="order in completedOrders" :key="order.orderNo" class="order-card completed">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status">已送达</span>
          </div>
          <div class="order-products">
            <div v-for="item in order.items" :key="item.productId" class="product-item">
              <span>{{ item.productName }}</span>
              <span>× {{ item.quantity }}</span>
            </div>
          </div>
          <div class="order-info">
            <span>📍 地址：{{ order.address || '未填写' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Delivery',
  data() {
    return {
      pendingOrders: [],
      shippingOrders: [],
      completedOrders: []
    }
  },
  mounted() {
    this.loadOrders()
    this.timer = setInterval(() => {
      this.loadOrders()
    }, 10000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    async loadOrders() {
      try {
        const res = await axios.get('http://localhost:8080/order/all')
        const allOrders = res.data || []
        
        const orderMap = {}
        for (const item of allOrders) {
          const orderNo = item.orderNo
          if (!orderMap[orderNo]) {
            orderMap[orderNo] = {
              orderNo: orderNo,
              status: item.status,
              address: item.remark || '未填写',
              items: []
            }
          }
          orderMap[orderNo].items.push({
            productId: item.productId,
            productName: item.productName || '商品#' + item.productId,
            quantity: 1
          })
        }
        
        const orderList = Object.values(orderMap)
        
        this.pendingOrders = orderList.filter(o => o.status === '待配送')
        this.shippingOrders = orderList.filter(o => o.status === '配送中')
        this.completedOrders = orderList.filter(o => o.status === '已送达')
      } catch (e) {
        console.error('加载订单失败', e)
      }
    },
    async acceptOrder(orderNo) {
      if (!confirm('确定要接下这个订单进行配送？')) return
      try {
        await axios.post('http://localhost:8080/order/status/update', {
          orderNo,
          status: '配送中'
        })
        alert('接单成功！开始配送')
        this.loadOrders()
      } catch (e) {
        alert('接单失败：' + (e.response?.data?.message || e.message))
      }
    },
    async completeOrder(orderNo) {
      if (!confirm('确定商品已送达？')) return
      try {
        await axios.post('http://localhost:8080/order/status/update', {
          orderNo,
          status: '已送达'
        })
        alert('订单已送达！')
        this.loadOrders()
      } catch (e) {
        alert('操作失败：' + (e.response?.data?.message || e.message))
      }
    }
  }
}
</script>

<style scoped>
.delivery-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.section {
  margin-bottom: 40px;
}
.section h3 {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
  color: #409eff;
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.empty {
  text-align: center;
  color: #999;
  padding: 30px;
  background: #f5f7fa;
  border-radius: 8px;
}
.order-card {
  border: 1px solid #e5e6eb;
  border-radius: 12px;
  padding: 20px;
  background: #fff;
  transition: all 0.3s;
}
.order-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.order-card.pending {
  border-left: 4px solid #67c23a;
}
.order-card.shipping {
  border-left: 4px solid #409eff;
}
.order-card.completed {
  border-left: 4px solid #67c23a;
  opacity: 0.7;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-no {
  font-weight: 600;
  color: #333;
}
.order-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
}
.pending .order-status {
  background: #f6ffed;
  color: #67c23a;
}
.shipping .order-status {
  background: #ecf5ff;
  color: #409eff;
}
.completed .order-status {
  background: #f0f9eb;
  color: #67c23a;
}
.order-products {
  margin-bottom: 12px;
}
.product-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px dashed #eee;
}
.product-item:last-child {
  border-bottom: none;
}
.order-info {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}
.order-actions {
  display: flex;
  justify-content: flex-end;
}
.accept-btn, .complete-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}
.accept-btn {
  background: #409eff;
  color: #fff;
}
.accept-btn:hover {
  background: #66b1ff;
}
.complete-btn {
  background: #67c23a;
  color: #fff;
}
.complete-btn:hover {
  background: #85ce61;
}
</style>
