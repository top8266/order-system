<template>
  <div class="container">
    <h2 class="page-title">商家后台管理系统</h2>

    <div class="func-bar">
      <button class="btn add-goods-btn" @click="showAddDialog = true">➕ 新增商品</button>
    </div>

    <div class="warning-card" v-if="warningList.length > 0">
      <h4>⚠️ 商品库存预警（库存低于10）</h4>
      <div class="warning-item" v-for="p in warningList" :key="p.id">
        {{ p.name }} ｜ 当前剩余库存：{{ p.stock }}
        <button class="btn-sm warn-btn" @click="openRestock(p)">立即补货</button>
      </div>
    </div>

    <div class="table-wrap">
      <h3 class="table-title">商品列表管理</h3>
      <table class="order-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>商品名称</th>
            <th>价格(¥)</th>
            <th>分类</th>
            <th>库存</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in goodsList" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.price }}</td>
            <td>{{ item.category }}</td>
            <td :class="item.stock < 10 ? 'red-text' : ''">{{ item.stock }}</td>
            <td>
              <button class="btn-sm success" @click="openRestock(item)">库存补货</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="stat-wrap">
      <div class="stat-card">
        <div class="num">{{ orders.length }}</div>
        <div class="text">全部订单</div>
      </div>
      <div class="stat-card">
        <div class="num wait">{{ orders.filter(o=>o.status=='待接单').length }}</div>
        <div class="text">待接单</div>
      </div>
      <div class="stat-card">
        <div class="num ok">{{ orders.filter(o=>o.status=='已完成').length }}</div>
        <div class="text">已完成</div>
      </div>
    </div>

    <div class="tool-bar">
      <button class="btn primary" @click="selectAll">全选</button>
      <button class="btn success" @click="batchComplete">批量完成</button>
      <button class="btn danger" @click="batchDelete">批量删除</button>
      <button class="btn default" @click="refreshAll">刷新数据</button>
    </div>

    <div class="table-wrap">
      <h3 class="table-title">订单列表管理</h3>
      <table class="order-table">
        <thead>
          <tr>
            <th width="60">选择</th>
            <th>订单ID</th>
            <th>关联商品ID</th>
            <th>订单状态</th>
            <th>用户备注</th>
            <th width="180">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td><input type="checkbox" v-model="selected" :value="order.id" class="check-box"></td>
            <td>{{ order.id }}</td>
            <td>{{ order.productId }}</td>
            <td>
              <span class="tag" :class="order.status=='待接单'?'tag-wait':'tag-ok'">
                {{ order.status }}
              </span>
            </td>
            <td>{{ order.remark || '无' }}</td>
            <td>
              <button class="btn-sm success" @click="complete(order.id)">完成</button>
              <button class="btn-sm danger" @click="del(order.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增商品弹窗（分类改为可输入） -->
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
          <label>商品分类（可选择/可手写）</label>
          <!-- 关键改动：改成可输入可选择 -->
          <input
            list="categoryOptions"
            v-model="newGoods.category"
            placeholder="选择或输入分类：水果/五金/文具等"
            style="width: 100%; padding: 10px; border-radius: 8px; border: 1px solid #ddd;"
          />
          <datalist id="categoryOptions">
            <option value="饮料">饮料</option>
            <option value="零食">零食</option>
            <option value="主食">主食</option>
            <option value="水果">水果</option>
            <option value="五金">五金</option>
            <option value="文具">文具</option>
            <option value="玩具">玩具</option>
          </datalist>
        </div>
      </div>
      <div class="dialog-foot">
        <button class="btn default" @click="showAddDialog=false">取消</button>
        <button class="btn primary" @click="submitAddGoods">提交添加</button>
      </div>
    </div>

    <!-- 补货弹窗 -->
    <div class="dialog-mask" v-if="showRestockDialog" @click="showRestockDialog=false"></div>
    <div class="dialog" v-if="showRestockDialog">
      <div class="dialog-head">
        <h3>商品补货 - {{ currGoods.name }}</h3>
        <button @click="showRestockDialog=false" class="close">×</button>
      </div>
      <div class="dialog-body">
        <div class="form-item">
          <label>当前库存：{{ currGoods.stock }}</label>
        </div>
        <div class="form-item">
          <label>补充库存数量</label>
          <input v-model.number="addStockNum" type="number" placeholder="请输入要补充的库存" />
        </div>
      </div>
      <div class="dialog-foot">
        <button class="btn default" @click="showRestockDialog=false">取消</button>
        <button class="btn primary" @click="submitRestock">确认补货</button>
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

      showAddDialog: false,
      newGoods: {
        name: '',
        price: 0,
        stock: 0,
        category: '' // 改为空，支持手写
      },

      showRestockDialog: false,
      currGoods: {},
      addStockNum: 0
    }
  },
  computed: {
    isAllSelected() {
      return this.selected.length === this.orders.length && this.orders.length>0
    }
  },
  methods: {
    async refreshAll() {
      await this.getGoodsList()
      await this.getOrderList()
    },
    async getGoodsList() {
      const {data} = await axios.get('http://localhost:8080/order/product/list')
      this.goodsList = data
      const {data:warn} = await axios.get('http://localhost:8080/order/product/warning')
      this.warningList = warn
    },
    async getOrderList() {
      const {data} = await axios.get('http://localhost:8080/order/list')
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

    async submitAddGoods() {
      if(!this.newGoods.name || !this.newGoods.price || !this.newGoods.stock || !this.newGoods.category){
        alert('请完善商品信息')
        return
      }
      await axios.post('http://localhost:8080/order/product/add', this.newGoods)
      alert('新增商品成功')
      this.showAddDialog = false
      this.newGoods = {name:'',price:0,stock:0,category:''}
      this.refreshAll()
    },

    selectAll() {
      this.selected = this.orders.map(o=>o.id)
    },
    async complete(id) {
      await axios.post(`http://localhost:8080/order/complete/${id}`)
      this.refreshAll()
    },
    async del(id) {
      await axios.post(`http://localhost:8080/order/delete/${id}`)
      this.refreshAll()
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
    }
  },
  mounted() {
    this.refreshAll()
  }
}
</script>

<style scoped>
/* 样式和之前完全一样，这里省略（和上一版相同） */
.container{
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
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
.func-bar{
  margin-bottom: 20px;
}
.add-goods-btn{
  background: #409eff;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
}
.warning-card{
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 25px;
}
.warning-card h4{
  color: #f56c6c;
  margin-bottom: 12px;
}
.warning-item{
  color: #c03636;
  margin: 8px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.warn-btn{
  background: #f56c6c;
  color: #fff;
  border: none;
  padding: 3px 8px;
  border-radius: 4px;
  cursor: pointer;
}
.table-title{
  margin: 10px 0 15px;
  color: #2a3342;
}
.red-text{
  color: #f56c6c;
  font-weight: 500;
}
.stat-wrap{
  display: flex;
  gap: 20px;
  margin: 30px 0 25px;
}
.stat-card{
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.stat-card .num{
  font-size: 32px;
  font-weight: 700;
  color: #333;
}
.stat-card .num.wait{
  color: #e6a23c;
}
.stat-card .num.ok{
  color: #67c23a;
}
.stat-card .text{
  color: #666;
  margin-top: 8px;
}
.tool-bar{
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.btn{
  padding: 8px 18px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}
.btn.primary{
  background: #409eff;
  color: #fff;
}
.btn.success{
  background: #67c23a;
  color: #fff;
}
.btn.danger{
  background: #f56c6c;
  color: #fff;
}
.btn.default{
  background: #e5e6eb;
  color: #333;
}
.table-wrap{
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}
.order-table{
  width: 100%;
  border-collapse: collapse;
}
.order-table th,.order-table td{
  padding: 14px 10px;
  text-align: center;
  border-bottom: 1px solid #eee;
}
.order-table th{
  background: #f5f7fa;
  font-weight: 600;
}
.check-box{
  cursor: pointer;
}
.tag{
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 13px;
}
.tag-wait{
  background: #fdf6ec;
  color: #e6a23c;
}
.tag-ok{
  background: #f0f9ff;
  color: #67c23a;
}
.btn-sm{
  padding: 5px 10px;
  border-radius: 6px;
  border: none;
  margin: 0 4px;
  cursor: pointer;
  font-size: 13px;
}
.btn-sm.success{
  background: #67c23a;
  color: #fff;
}
.btn-sm.danger{
  background: #f56c6c;
  color: #fff;
}
.dialog-mask{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 999;
}
.dialog{
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%,-50%);
  width: 420px;
  background: #fff;
  border-radius: 12px;
  z-index: 1000;
  overflow: hidden;
}
.dialog-head{
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  font-size: 18px;
  font-weight: 600;
}
.close{
  border: none;
  background: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}
.dialog-body{
  padding: 25px;
}
.form-item{
  margin-bottom: 18px;
}
.form-item label{
  display: block;
  margin-bottom: 6px;
  color: #333;
}
.form-item input,.form-item select{
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
}
.dialog-foot{
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>