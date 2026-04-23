<template>
  <div class="container">
    <h2>订单管理系统 - 管理员后台</h2>

    <!-- 订单统计卡片 -->
    <div class="stats">
      <div class="stat-card">
        <h3>{{ stats.total }}</h3>
        <p>总订单数</p>
      </div>
      <div class="stat-card">
        <h3 style="color: orange">{{ stats.pending }}</h3>
        <p>待接单</p>
      </div>
      <div class="stat-card">
        <h3 style="color: green">{{ stats.completed }}</h3>
        <p>已完成</p>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="toolbar">
      <button @click="selectAll" class="btn-select">全选</button>
      <button @click="batchComplete" class="btn-complete" :disabled="selectedIds.length === 0">批量完成</button>
      <button @click="batchDelete" class="btn-delete" :disabled="selectedIds.length === 0">批量删除</button>
      <button @click="refreshOrders" class="btn-refresh">刷新</button>
    </div>

    <!-- 订单列表 -->
    <table class="order-table">
      <thead>
        <tr>
          <th><input type="checkbox" @change="toggleAll" :checked="isAllSelected"></th>
          <th>订单编号</th>
          <th>商品</th>
          <th>用户</th>
          <th>状态</th>
          <th>备注</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="order in orders" :key="order.id">
          <td><input type="checkbox" v-model="selectedIds" :value="order.id"></td>
          <td>订单 #{{ order.id }}</td>
          <td>商品 #{{ order.productId }}</td>
          <td>用户 #{{ order.userId }}</td>
          <td>{{ order.status }}</td>
          <td>{{ order.remark || '无' }}</td>
          <td>
            <button @click="completeOrder(order.id)" class="btn-small btn-complete">完成</button>
            <button @click="deleteOrder(order.id)" class="btn-small btn-delete">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return {
      orders: [],
      selectedIds: []
    };
  },
  computed: {
    stats() {
      const total = this.orders.length;
      const pending = this.orders.filter(o => o.status === '待接单').length;
      const completed = this.orders.filter(o => o.status === '已完成').length;
      return { total, pending, completed };
    },
    isAllSelected() {
      return this.selectedIds.length === this.orders.length && this.orders.length > 0;
    }
  },
  methods: {
    async refreshOrders() {
      const res = await axios.get("http://localhost:8080/order/list");
      this.orders = res.data;
      this.selectedIds = []; // 刷新后清空选中
    },
    toggleAll(e) {
      if (e.target.checked) {
        this.selectedIds = this.orders.map(o => o.id);
      } else {
        this.selectedIds = [];
      }
    },
    selectAll() {
      this.selectedIds = this.orders.map(o => o.id);
    },
    async completeOrder(id) {
      await axios.post(`http://localhost:8080/order/complete/${id}`);
      this.refreshOrders();
    },
    async deleteOrder(id) {
      await axios.post(`http://localhost:8080/order/delete/${id}`);
      this.refreshOrders();
    },
    async batchComplete() {
      await axios.post("http://localhost:8080/order/batchComplete", this.selectedIds);
      this.refreshOrders();
      alert("批量完成成功！");
    },
    async batchDelete() {
      if (confirm("确定要删除选中订单吗？")) {
        await axios.post("http://localhost:8080/order/batchDelete", this.selectedIds);
        this.refreshOrders();
        alert("批量删除成功！");
      }
    }
  },
  mounted() {
    this.refreshOrders();
  }
};
</script>

<style scoped>
.container {
  padding: 20px;
}
.stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  flex: 1;
  border: 1px solid #eee;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
}
.toolbar {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}
.btn-select, .btn-refresh {
  background: #666;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.btn-complete {
  background: #52c41a;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.btn-delete {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.btn-small {
  padding: 4px 8px;
  font-size: 12px;
  margin-right: 5px;
}
.order-table {
  width: 100%;
  border-collapse: collapse;
}
.order-table th, .order-table td {
  border: 1px solid #eee;
  padding: 10px;
  text-align: center;
}
</style>