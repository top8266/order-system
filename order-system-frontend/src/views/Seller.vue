<template>
  <div class="seller-container">
    <div class="page-header">
      <h1>商家管理后台</h1>
      <button class="btn-add" @click="showAdd=true">➕ 新增商品</button>
    </div>

    <!-- 库存预警 -->
    <div class="card" v-if="warning.length">
      <h3 class="card-title warning">⚠️ 库存预警</h3>
      <div class="warning-list">
        <div v-for="p in warning" :key="p.id">
          <span>{{ p.name }}</span>
          <span class="badge-warning">库存：{{ p.stock }}</span>
          <button class="btn-mini blue" @click="toRestock(p)">补货</button>
        </div>
      </div>
    </div>

    <!-- 商品管理 -->
    <div class="card">
      <h3 class="card-title">📦 商品管理</h3>
      <div class="table-box">
        <table class="data-table">
          <thead>
            <tr>
              <th>商品名称</th>
              <th>价格</th>
              <th>分类</th>
              <th>库存</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in goods" :key="p.id">
              <td>{{ p.name }}</td>
              <td class="text-price">¥{{ p.price }}</td>
              <td>{{ p.category }}</td>
              <td :class="p.stock < 10 ? 'text-danger' : 'text-normal'">
                {{ p.stock }}
              </td>
              <td>
                <button class="btn-mini blue" @click="toRestock(p)">补货</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 订单管理 -->
    <div class="card">
      <h3 class="card-title">📋 订单管理</h3>
      <div class="tool-bar">
        <button class="btn-default" @click="checkAll">全选</button>
        <button class="btn-blue" @click="doneBatch">批量完成</button>
        <button class="btn-red" @click="delBatch">批量删除</button>
        <button class="btn-default" @click="refresh">刷新</button>
      </div>
      <div class="table-box">
        <table class="data-table">
          <thead>
            <tr>
              <th>选择</th>
              <th>订单号</th>
              <th>状态</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in orders" :key="o.id">
              <td><input type="checkbox" v-model="checked" :value="o.id" /></td>
              <td>{{ o.id }}</td>
              <td>
                <span :class="o.status === '已完成' ? 'tag-success' : 'tag-wait'">
                  {{ o.status }}
                </span>
              </td>
              <td>{{ o.remark || '无' }}</td>
              <td class="action-btns">
                <button class="btn-mini blue" @click="doneOne(o.id)">完成</button>
                <button class="btn-mini red" @click="delOne(o.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新增商品弹窗 -->
    <div class="modal-mask" v-if="showAdd" @click="showAdd=false"></div>
    <div class="modal" v-if="showAdd">
      <h3>新增商品</h3>
      <div class="form-group">
        <label>商品名称</label>
        <input v-model="form.name" placeholder="请输入商品名称" />
      </div>
      <div class="form-group">
        <label>商品价格（元）</label>
        <input v-model.number="form.price" type="number" step="0.01" placeholder="例如：9.9" />
      </div>
      <div class="form-group">
        <label>初始库存</label>
        <input v-model.number="form.stock" type="number" placeholder="例如：100" />
      </div>
      <div class="form-group">
        <label>商品分类</label>
        <input v-model="form.category" placeholder="水果/零食/饮料/文具/五金/日化" />
      </div>
      <button class="modal-btn" @click="addGoods">确认添加</button>
    </div>

    <!-- 补货弹窗 -->
    <div class="modal-mask" v-if="showRestock" @click="showRestock=false"></div>
    <div class="modal" v-if="showRestock">
      <h3>商品补货</h3>
      <div class="form-group">
        <label>商品名称：{{ curr.name }}</label>
        <input v-model.number="addNum" type="number" placeholder="请输入补货数量" />
      </div>
      <button class="modal-btn" @click="doRestock">确认补货</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return {
      goods: [],
      orders: [],
      warning: [],
      checked: [],
      showAdd: false,
      showRestock: false,
      form: { name: '', price: 0, stock: 0, category: '' },
      curr: {},
      addNum: 0
    }
  },
  methods: {
    async refresh() {
      const { data: g } = await axios.get('http://localhost:8080/order/product/list')
      this.goods = g
      const { data: w } = await axios.get('http://localhost:8080/order/product/warning')
      this.warning = w
      const { data: o } = await axios.get('http://localhost:8080/order/list')
      this.orders = o
      this.checked = []
    },
    toRestock(p) {
      this.curr = p
      this.addNum = 0
      this.showRestock = true
    },
    async doRestock() {
      await axios.post('http://localhost:8080/order/product/restock', {
        id: this.curr.id,
        stock: this.addNum
      })
      this.showRestock = false
      this.refresh()
    },
    async addGoods() {
      await axios.post('http://localhost:8080/order/product/add', this.form)
      this.showAdd = false
      this.form = { name: '', price: 0, stock: 0, category: '' }
      this.refresh()
    },
    checkAll() {
      this.checked = this.orders.map(x => x.id)
    },
    async doneOne(id) {
      await axios.post(`http://localhost:8080/order/complete/${id}`)
      this.refresh()
    },
    async delOne(id) {
      await axios.post(`http://localhost:8080/order/delete/${id}`)
      this.refresh()
    },
    async doneBatch() {
      await axios.post('http://localhost:8080/order/batchComplete', this.checked)
      this.refresh()
    },
    async delBatch() {
      await axios.post('http://localhost:8080/order/batchDelete', this.checked)
      this.refresh()
    }
  },
  mounted() {
    this.refresh()
  }
}
</script>

<style scoped>
/* 整体布局 */
.seller-container {
  background: #f5f7fa;
  min-height: 100vh;
  padding: 30px 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: "Microsoft YaHei", sans-serif;
}

/* 顶部标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h1 {
  font-size: 26px;
  color: #222;
  margin: 0;
}

/* 卡片 */
.card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.card-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.warning {
  color: #ff4d4f;
}

/* 预警列表 */
.warning-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.warning-list > div {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #fff7f8;
  border-radius: 8px;
}
.badge-warning {
  background: #ff4d4f;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

/* 表格容器 + 滚动条 */
.table-box {
  max-height: 380px;
  overflow-y: auto;
  border-radius: 12px;
  border: 1px solid #eee;
}
.table-box::-webkit-scrollbar {
  width: 6px;
}
.table-box::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

/* 表格样式 */
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th {
  background: #f9fafb;
  padding: 14px 12px;
  text-align: center;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid #eee;
}
.data-table td {
  padding: 14px 12px;
  text-align: center;
  color: #555;
  border-bottom: 1px solid #f4f4f4;
}
.data-table tr:hover {
  background: #fafbfc;
}

/* 文字颜色 */
.text-price {
  color: #ff7d00 !important;
  font-weight: bold;
}
.text-danger {
  color: #ff4d4f !important;
  font-weight: bold;
}
.text-normal {
  color: #00b42a !important;
}

/* 标签 */
.tag-success {
  background: #e6ffed;
  color: #00b42a;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
}
.tag-wait {
  background: #fff7e6;
  color: #ff7d00;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
}

/* 按钮 */
.btn-add {
  background: #1677ff;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.2s;
}
.btn-add:hover {
  background: #0958d6;
}

.tool-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.btn-default {
  padding: 6px 14px;
  border: 1px solid #dcdcdc;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}
.btn-blue {
  padding: 6px 14px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.btn-red {
  padding: 6px 14px;
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-mini {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  margin: 0 2px;
}
.btn-mini.blue {
  background: #1677ff;
  color: #fff;
}
.btn-mini.red {
  background: #ff4d4f;
  color: #fff;
}

.action-btns {
  display: flex;
  gap: 6px;
  justify-content: center;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
}
.modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #fff;
  width: 380px;
  padding: 30px;
  border-radius: 16px;
  z-index: 100;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}
.modal h3 {
  text-align: center;
  margin: 0 0 24px 0;
  color: #333;
}
.form-group {
  margin-bottom: 18px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}
.modal-btn {
  width: 100%;
  padding: 11px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  margin-top: 10px;
}
</style>