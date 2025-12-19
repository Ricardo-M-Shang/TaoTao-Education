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
            <el-tag :type="statusType(o.status)" size="small" effect="plain" class="status-tag">{{ statusText(o.status) }}</el-tag>
            <div class="actions">
              <template v-if="isPaidStatus(o.status)">
                <el-button type="primary" round size="small" @click="goStudy(o)">去学习</el-button>
                <el-button round size="small" @click="showDetail(o)">订单详情</el-button>
              </template>
              <template v-else-if="isPendingStatus(o.status)">
                <el-button type="danger" round size="small" @click="pay(o)">立即支付</el-button>
                <el-button round size="small" @click="cancel(o)">取消订单</el-button>
                <el-button round size="small" @click="showDetail(o)">订单详情</el-button>
              </template>
              <template v-else>
                <el-button round size="small" @click="showDetail(o)">订单详情</el-button>
              </template>
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
        <el-button v-if="currentOrder && isPendingStatus(currentOrder.status)" type="danger" @click="pay(currentOrder!); detailVisible = false">立即支付</el-button>
        <el-button v-if="currentOrder && isPaidStatus(currentOrder.status)" type="primary" @click="goStudy(currentOrder!); detailVisible = false">去学习</el-button>
      </template>
    </el-dialog>

    <!-- 支付方式选择弹窗 -->
    <el-dialog v-model="paymentVisible" title="选择支付方式" width="450px" :close-on-click-modal="false">
      <div class="payment-dialog" v-if="orderToPay">
        <div class="pay-info">
          <div class="pay-amount-label">应付金额</div>
          <div class="pay-amount-value">¥{{ orderToPay.payAmount }}</div>
          <div class="pay-course">{{ orderToPay.courseTitle }}</div>
        </div>
        
        <div class="payment-methods">
          <div 
            class="payment-item" 
            :class="{ active: selectedPayType === 1 }"
            @click="selectedPayType = 1"
          >
            <div class="payment-icon alipay">
              <svg viewBox="0 0 1024 1024" width="32" height="32">
                <path d="M1023.795 701.811c-23.47-71.898-318.74-478.268-318.74-478.268s23.881 174.08-2.868 326.034c-100.659-49.306-201.932-98.509-302.387-148.326 62.72-120.78 111.82-261.862 125.389-379.251h-174.49v-22h200.494v-44.512h-200.494v-89.742c0-22-22-22-22-22h-89.024s-22 0-22 22v89.742h-200.494v44.512h200.494v22h-174.695c20.826 168.448 114.483 360.55 210.483 498.79-82.714 36.864-258.714 105.37-242.304 195.277 14.95 102.502 137.318 121.19 239.82 58.47 79.155-48.435 150.118-122.214 213.76-207.667 113.664 55.296 227.84 111.001 341.811 166.195-34.202 148.634-137.728 255.386-289.477 255.386-158.31 0-286.814-128.512-286.814-286.814h-133.53c0 231.885 188.262 420.147 420.147 420.147 230.451 0 418.202-185.446 423.27-415.283 1.024-14.95-0.41-29.286-5.376-44.717v0zM378.88 887.194c-79.77 48.23-164.506 17.613-172.032-30.72-9.011-57.037 75.418-116.634 185.856-157.389-49.92 79.565-91.597 137.523-167.27 188.109h153.447zM592.947 562.79c80.384 39.117 161.382 77.824 242.304 116.838-76.647 10.854-152.474 20.48-228.71 30.106 56.32-79.565 96.87-146.739 139.878-217.702-48.742 24.064-100.454 47.718-153.472 70.758v0z" fill="#009FE8"></path>
              </svg>
            </div>
            <div class="payment-info">
              <div class="payment-name">支付宝支付</div>
              <div class="payment-desc">推荐使用支付宝支付</div>
            </div>
            <div class="payment-check">
              <el-icon v-if="selectedPayType === 1" color="#409eff"><CircleCheck /></el-icon>
            </div>
          </div>
          
          <div 
            class="payment-item" 
            :class="{ active: selectedPayType === 2 }"
            @click="selectedPayType = 2"
          >
            <div class="payment-icon wechat">
              <svg viewBox="0 0 1024 1024" width="32" height="32">
                <path d="M664.250054 368.541681c10.015098 0 19.892049 0.732687 29.67281 1.795902-26.647917-122.810047-159.358451-214.077703-310.826188-214.077703-169.353083 0-308.085774 114.232694-308.085774 259.274968 0 83.708098 46.165436 152.460344 123.281791 205.78483l-30.80868 91.730191 106.713294-53.208485c38.51328 7.531001 69.36134 15.195513 107.924427 15.195513 9.66111 0 19.230874-0.420091 28.752784-1.225921-6.025227-20.36584-9.476251-41.723264-9.476251-63.862493C402.328693 476.632491 517.908345 368.541681 664.250054 368.541681zM498.62897 285.87389c23.200398 0 38.801966 15.006397 38.801966 38.027625 0 22.864778-15.601568 38.42779-38.801966 38.42779-23.058574 0-46.306838-15.563012-46.306838-38.42779C452.322132 300.880287 475.570396 285.87389 498.62897 285.87389zM283.016307 362.329305c-23.058574 0-46.448565-15.563012-46.448565-38.42779 0-23.021228 23.389992-38.027625 46.448565-38.027625 23.107465 0 38.709033 15.006397 38.709033 38.027625C321.725339 346.766293 306.123772 362.329305 283.016307 362.329305zM945.448458 606.151333c0-121.888048-123.258325-221.236753-261.683178-221.236753-146.57838 0-262.015407 99.348706-262.015407 221.236753 0 122.06508 115.437027 221.200938 262.015407 221.200938 30.66644 0 61.617359-7.62863 92.423993-15.284959l84.513836 45.850549-23.248652-76.312618C899.379213 735.776599 945.448458 674.90216 945.448458 606.151333zM598.803483 567.994292c-15.332473 0-30.807999-15.200189-30.807999-30.66644 0-15.332473 15.475526-30.948796 30.807999-30.948796 23.295743 0 38.709033 15.616323 38.709033 30.948796C637.512516 552.794103 622.099226 567.994292 598.803483 567.994292zM768.25071 567.994292c-15.192666 0-30.594949-15.200189-30.594949-30.66644 0-15.332473 15.402283-30.948796 30.594949-30.948796 23.107465 0 38.613688 15.616323 38.613688 30.948796C806.864398 552.794103 791.358175 567.994292 768.25071 567.994292z" fill="#00C800"></path>
              </svg>
            </div>
            <div class="payment-info">
              <div class="payment-name">微信支付</div>
              <div class="payment-desc">使用微信扫码支付</div>
            </div>
            <div class="payment-check">
              <el-icon v-if="selectedPayType === 2" color="#409eff"><CircleCheck /></el-icon>
            </div>
          </div>
        </div>

        <div class="payment-tip">
          <el-alert 
            title="提示：这是模拟支付环境，点击确认支付后订单将直接完成支付" 
            type="info" 
            :closable="false"
            show-icon
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="paymentVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay" :loading="paying">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, CircleCheck } from '@element-plus/icons-vue'
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

// 支付相关
const paymentVisible = ref(false)
const orderToPay = ref<OrderInfo | null>(null)
const selectedPayType = ref(1) // 默认支付宝
const paying = ref(false)

const statusType = (s: number) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info') as 'warning' | 'success' | 'info' | 'danger'
const statusText = (s: number) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }[s] || '未知')
const payTypeText = (t: number) => ({ 1: '支付宝', 2: '微信支付', 3: '余额支付' }[t] || '其他')

// 辅助函数：判断是否为待支付状态（支持字符串和数字）
const isPendingStatus = (status: any): boolean => {
  const numStatus = Number(status)
  return numStatus === 0
}

// 辅助函数：判断是否为已支付状态（支持字符串和数字）
const isPaidStatus = (status: any): boolean => {
  const numStatus = Number(status)
  return numStatus === 1
}

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
    // 确保status字段为数字类型
    orders.value = (res.data.records || []).map((order: any) => ({
      ...order,
      status: Number(order.status),
      payType: Number(order.payType || 0),
      payAmount: Number(order.payAmount || 0),
      originalPrice: Number(order.originalPrice || 0),
      discountAmount: Number(order.discountAmount || 0)
    }))
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function pay(o: OrderInfo) {
  orderToPay.value = o
  selectedPayType.value = 1 // 重置为支付宝
  paymentVisible.value = true
}

async function confirmPay() {
  if (!orderToPay.value) return
  
  paying.value = true
  try {
    await payOrder({ orderNo: orderToPay.value.orderNo, payType: selectedPayType.value })
    ElMessage.success('支付成功！')
    paymentVisible.value = false
    orderToPay.value = null
    load()
  } catch (error) {
    ElMessage.error('支付失败，请重试')
  } finally {
    paying.value = false
  }
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
    align-items: center;
    gap: 10px;
    
    .el-button { margin: 0; }
  }
}

.status-tag { margin: 0 10px; }

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

// 支付方式选择弹窗样式
.payment-dialog {
  .pay-info {
    text-align: center;
    padding: 20px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    margin-bottom: 20px;
    color: #fff;
    
    .pay-amount-label {
      font-size: 12px;
      opacity: 0.9;
      margin-bottom: 8px;
    }
    
    .pay-amount-value {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 6px;
    }
    
    .pay-course {
      font-size: 13px;
      opacity: 0.85;
    }
  }
  
  .payment-methods {
    margin-bottom: 16px;
  }
  
  .payment-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border: 2px solid #e5e7eb;
    border-radius: 8px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      border-color: #d1d5db;
      background: #f9fafb;
    }
    
    &.active {
      border-color: #409eff;
      background: #ecf5ff;
    }
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .payment-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    
    &.alipay {
      background: #e6f7ff;
    }
    
    &.wechat {
      background: #f0fdf4;
    }
  }
  
  .payment-info {
    flex: 1;
    
    .payment-name {
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .payment-desc {
      font-size: 12px;
      color: var(--text-muted);
    }
  }
  
  .payment-check {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }
  
  .payment-tip {
    :deep(.el-alert) {
      padding: 10px 12px;
      
      .el-alert__title {
        font-size: 12px;
      }
    }
  }
}
</style>
