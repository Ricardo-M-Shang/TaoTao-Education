<template>
  <div class="orders-page">
    <div class="container">
      <h2>我的订单</h2>
      <el-tabs v-model="activeTab" @tab-change="load">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="pending" />
        <el-tab-pane label="已支付" name="paid" />
      </el-tabs>
      <div class="list" v-loading="loading">
        <div v-for="o in orders" :key="o.id" class="item">
          <div class="top"><span>{{ o.orderNo }}</span><span>{{ o.createTime }}</span></div>
          <div class="body">
            <img :src="o.courseCover || defaultCover" />
            <div class="info"><h4>{{ o.courseTitle }}</h4><p>{{ o.teacherName }}</p></div>
            <div class="price">¥{{ o.payAmount }}</div>
            <el-tag :type="statusType(o.status)" size="small">{{ statusText(o.status) }}</el-tag>
            <div class="actions">
              <el-button v-if="o.status === 0" type="primary" size="small" @click="pay(o)">支付</el-button>
              <el-button v-if="o.status === 0" size="small" @click="cancel(o)">取消</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && !orders.length" description="暂无订单" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, cancelOrder, payOrder } from '@/api/order'
import type { OrderInfo } from '@/types/order'

const loading = ref(false)
const activeTab = ref('all')
const orders = ref<OrderInfo[]>([])
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'

const statusType = (s: number) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info')
const statusText = (s: number) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }[s] || '未知')

async function load() {
  loading.value = true
  const status = activeTab.value === 'pending' ? 0 : activeTab.value === 'paid' ? 1 : undefined
  try { const res = await getOrderList({ status, pageNum: 1, pageSize: 20 }); orders.value = res.data.records }
  catch (e) { console.error(e) } finally { loading.value = false }
}

async function pay(o: OrderInfo) { ElMessageBox.confirm('确定支付？').then(async () => { await payOrder({ orderNo: o.orderNo, payType: 1 }); ElMessage.success('支付成功'); load() }).catch(() => {}) }
async function cancel(o: OrderInfo) { ElMessageBox.confirm('确定取消？').then(async () => { await cancelOrder(o.orderNo); ElMessage.success('已取消'); load() }).catch(() => {}) }

onMounted(load)
</script>

<style lang="scss" scoped>
.orders-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 800px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 10px; }
h2 { font-size: 15px; margin-bottom: 12px; }
.item { border: 1px solid #eee; border-radius: 8px; margin-bottom: 12px; overflow: hidden; }
.top { padding: 8px 12px; background: #f8fafc; display: flex; justify-content: space-between; font-size: 10px; color: var(--text-muted); }
.body { padding: 12px; display: flex; align-items: center; gap: 12px; }
.body img { width: 90px; height: 50px; object-fit: cover; border-radius: 6px; }
.body .info { flex: 1; h4 { font-size: 12px; margin-bottom: 4px; } p { font-size: 10px; color: var(--text-muted); } }
.body .price { font-size: 14px; font-weight: 700; color: #ef4444; width: 70px; text-align: center; }
.body .actions { display: flex; gap: 6px; }
</style>
