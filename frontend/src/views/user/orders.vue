<template>
  <div class="orders-page">
    <div class="container">
      <h2>我的订单</h2>
      <el-tabs v-model="activeTab" @tab-change="load">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="pending" />
        <el-tab-pane label="已支付" name="paid" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>
      <div class="list" v-loading="loading">
        <div v-for="o in orders" :key="o.id" class="item">
          <div class="top">
            <span class="order-no">订单号：{{ o.orderNo }}</span>
            <span class="create-time">{{ formatTime(o.createTime) }}</span>
          </div>
          <div class="body">
            <img :src="o.courseCover || defaultCover" @click="$router.push(`/course/${o.courseId}`)" />
            <div class="info">
              <h4 @click="$router.push(`/course/${o.courseId}`)">{{ o.courseTitle }}</h4>
              <p>{{ o.teacherName }}</p>
              <div class="price-info">
                <span class="pay-amount">¥{{ o.payAmount }}</span>
                <del v-if="o.discountAmount > 0" class="original">¥{{ o.originalPrice }}</del>
              </div>
            </div>
            <el-tag :type="statusType(o.status)" size="small">{{ statusText(o.status) }}</el-tag>
            <div class="actions">
              <el-button v-if="o.status === 1" type="success" size="small" @click="goStudy(o)">去学习</el-button>
              <el-button v-if="o.status === 0" type="primary" size="small" @click="pay(o)">立即支付</el-button>
              <el-button v-if="o.status === 0" size="small" @click="cancel(o)">取消订单</el-button>
              <el-button size="small" text type="primary" @click="showDetail(o)">订单详情</el-button>
            </div>
          </div>
          <!-- 待支付倒计时 -->
          <div class="expire-tip" v-if="o.status === 0 && o.expireTime">
            <el-icon><Clock /></el-icon>
            <span>请在 {{ getExpireCountdown(o.expireTime) }} 内完成支付，超时订单将自动取消</span>
          </div>
        </div>
        <el-empty v-if="!loading && !orders.length" description="暂无订单">
          <el-button type="primary" size="small" @click="$router.push('/course')">去选课</el-button>
        </el-empty>
      </div>
      
      <!-- 分页 -->
      <div class="pager" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="load"
        />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="500px" :close-on-click-modal="false">
      <div class="order-detail" v-if="currentOrder">
        <div class="detail-section">
          <h4>订单信息</h4>
          <div class="detail-row">
            <span class="label">订单编号</span>
            <span class="value">{{ currentOrder.orderNo }}</span>
          </div>
          <div class="detail-row">
            <span class="label">订单状态</span>
            <el-tag :type="statusType(currentOrder.status)" size="small">{{ statusText(currentOrder.status) }}</el-tag>
          </div>
          <div class="detail-row">
            <span class="label">创建时间</span>
            <span class="value">{{ currentOrder.createTime }}</span>
          </div>
          <div class="detail-row" v-if="currentOrder.payTime">
            <span class="label">支付时间</span>
            <span class="value">{{ currentOrder.payTime }}</span>
          </div>
          <div class="detail-row" v-if="currentOrder.payType && currentOrder.status === 1">
            <span class="label">支付方式</span>
            <span class="value">{{ payTypeText(currentOrder.payType) }}</span>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>课程信息</h4>
          <div class="course-info">
            <img :src="currentOrder.courseCover || defaultCover" />
            <div class="course-detail">
              <h5>{{ currentOrder.courseTitle }}</h5>
              <p>讲师：{{ currentOrder.teacherName }}</p>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>价格明细</h4>
          <div class="detail-row">
            <span class="label">课程原价</span>
            <span class="value">¥{{ currentOrder.originalPrice }}</span>
          </div>
          <div class="detail-row" v-if="currentOrder.discountAmount > 0">
            <span class="label">优惠金额</span>
            <span class="value discount">-¥{{ currentOrder.discountAmount }}</span>
          </div>
          <div class="detail-row total">
            <span class="label">实付金额</span>
            <span class="value pay-amount">¥{{ currentOrder.payAmount }}</span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentOrder?.status === 0" type="primary" @click="pay(currentOrder!); detailVisible = false">立即支付</el-button>
        <el-button v-if="currentOrder?.status === 1" type="success" @click="goStudy(currentOrder!); detailVisible = false">去学习</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import { getOrderList, cancelOrder, payOrder } from '@/api/order'
import type { OrderInfo } from '@/types/order'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const orders = ref<OrderInfo[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'

// 订单详情
const detailVisible = ref(false)
const currentOrder = ref<OrderInfo | null>(null)

const statusType = (s: number) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info') as 'warning' | 'success' | 'info' | 'danger'
const statusText = (s: number) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }[s] || '未知')
const payTypeText = (t: number) => ({ 1: '支付宝', 2: '微信支付', 3: '余额支付' }[t] || '其他')

function formatTime(time: string): string {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

function getExpireCountdown(expireTime: string): string {
  if (!expireTime) return ''
  const expire = new Date(expireTime).getTime()
  const now = Date.now()
  const diff = expire - now
  
  if (diff <= 0) return '已超时'
  
  const minutes = Math.floor(diff / (60 * 1000))
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  
  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  }
  return `${mins}分钟`
}

function showDetail(order: OrderInfo) {
  currentOrder.value = order
  detailVisible.value = true
}

function goStudy(order: OrderInfo) {
  router.push(`/study/${order.courseId}`)
}

async function load() {
  loading.value = true
  let status: number | undefined
  if (activeTab.value === 'pending') status = 0
  else if (activeTab.value === 'paid') status = 1
  else if (activeTab.value === 'cancelled') status = 2
  
  try {
    const res = await getOrderList({ status, pageNum: pageNum.value, pageSize: pageSize.value })
    orders.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function pay(o: OrderInfo) {
  ElMessageBox.confirm('确定支付该订单？', '支付确认', {
    confirmButtonText: '确定支付',
    cancelButtonText: '取消'
  }).then(async () => {
    await payOrder({ orderNo: o.orderNo, payType: 1 })
    ElMessage.success('支付成功')
    load()
  }).catch(() => {})
}

async function cancel(o: OrderInfo) {
  ElMessageBox.confirm('确定取消该订单？', '取消确认', {
    confirmButtonText: '确定取消',
    cancelButtonText: '返回',
    type: 'warning'
  }).then(async () => {
    await cancelOrder(o.orderNo)
    ElMessage.success('订单已取消')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>

<style lang="scss" scoped>
.orders-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 900px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 10px; }
h2 { font-size: 15px; margin-bottom: 12px; }

.item { 
  border: 1px solid #eee; 
  border-radius: 10px; 
  margin-bottom: 14px; 
  overflow: hidden;
  transition: all 0.2s;
  
  &:hover {
    border-color: #ddd;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  }
}

.top { 
  padding: 10px 14px; 
  background: #f8fafc; 
  display: flex; 
  justify-content: space-between; 
  align-items: center;
  
  .order-no {
    font-size: 11px;
    color: var(--text-secondary);
    font-family: monospace;
  }
  
  .create-time {
    font-size: 11px;
    color: var(--text-muted);
  }
}

.body { 
  padding: 14px; 
  display: flex; 
  align-items: center; 
  gap: 14px;
  
  img { 
    width: 100px; 
    height: 60px; 
    object-fit: cover; 
    border-radius: 6px;
    cursor: pointer;
    transition: transform 0.2s;
    
    &:hover { transform: scale(1.02); }
  }
  
  .info { 
    flex: 1; 
    min-width: 0;
    
    h4 { 
      font-size: 13px; 
      margin-bottom: 4px;
      cursor: pointer;
      
      &:hover { color: var(--primary-color); }
    }
    
    p { font-size: 11px; color: var(--text-muted); margin-bottom: 6px; }
    
    .price-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .pay-amount {
        font-size: 15px;
        font-weight: 700;
        color: #ef4444;
      }
      
      .original {
        font-size: 11px;
        color: #999;
      }
    }
  }
  
  .actions { 
    display: flex; 
    flex-direction: column;
    gap: 6px;
    min-width: 90px;
    
    .el-button { width: 100%; }
  }
}

.expire-tip {
  padding: 8px 14px;
  background: #fff7ed;
  border-top: 1px solid #fed7aa;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #c2410c;
  
  .el-icon { font-size: 14px; }
}

.pager { margin-top: 20px; display: flex; justify-content: center; }

// 订单详情弹窗样式
.order-detail {
  .detail-section {
    margin-bottom: 20px;
    
    &:last-child { margin-bottom: 0; }
    
    h4 {
      font-size: 13px;
      font-weight: 600;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid #f0f0f0;
    }
  }
  
  .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    
    .label {
      font-size: 12px;
      color: var(--text-muted);
    }
    
    .value {
      font-size: 12px;
      color: var(--text-primary);
      
      &.discount { color: #10b981; }
      &.pay-amount { font-size: 16px; font-weight: 700; color: #ef4444; }
    }
    
    &.total {
      padding-top: 12px;
      margin-top: 8px;
      border-top: 1px dashed #e0e0e0;
    }
  }
  
  .course-info {
    display: flex;
    gap: 12px;
    padding: 12px;
    background: #f8fafc;
    border-radius: 8px;
    
    img {
      width: 80px;
      height: 50px;
      object-fit: cover;
      border-radius: 6px;
    }
    
    .course-detail {
      flex: 1;
      
      h5 {
        font-size: 13px;
        font-weight: 500;
        margin-bottom: 4px;
      }
      
      p {
        font-size: 11px;
        color: var(--text-muted);
      }
    }
  }
}
</style>
