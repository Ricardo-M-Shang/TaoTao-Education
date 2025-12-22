<template>
  <div class="seckill-page">
    <div class="banner">
      <div class="banner-content">
        <h1>限时秒杀</h1>
        <p>每日精选好券，限时限量抢购</p>
      </div>
    </div>
    
    <div class="container">
      <div v-if="loading" class="loading-box">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="!coupons.length" class="empty-box">
        <el-empty description="暂无秒杀活动" />
      </div>
      
      <div v-else class="coupon-list">
        <div v-for="c in coupons" :key="c.id" class="seckill-card">
          <div class="left-section">
            <div class="amount" v-if="c.type === 1">
              <span class="symbol">¥</span>
              <span class="num">{{ c.discountAmount }}</span>
            </div>
            <div class="amount" v-else>
              <span class="num">{{ ((c.discountRate || 1) * 10).toFixed(1) }}</span>
              <span class="symbol">折</span>
            </div>
            <div class="condition">满 {{ c.thresholdAmount }} 可用</div>
          </div>
          
          <div class="middle-section">
            <h3 class="title">{{ c.name }}</h3>
            <div class="time-range">
              <el-icon><Timer /></el-icon>
              {{ formatTime(c.grabStartTime) }} 开抢
            </div>
            <div class="progress-box">
              <el-progress 
                :percentage="calcPercentage(c)" 
                :status="c.stock === 0 ? 'exception' : ''"
                :stroke-width="10"
              />
              <span class="stock-text">剩余 {{ c.stock }} 张</span>
            </div>
          </div>
          
          <div class="right-section">
            <!-- 倒计时 -->
            <div v-if="getSeckillStatus(c) === 'pending'" class="countdown">
              <p>距离开始</p>
              <div class="timer">
                {{ c.countdownStr }}
              </div>
            </div>
            
            <el-button 
              type="primary" 
              size="large" 
              round
              :disabled="getSeckillStatus(c) !== 'active'"
              :loading="c.seckillLoading"
              @click="handleSeckill(c)"
            >
              {{ getBtnText(c) }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getSeckillCoupons, doSeckill } from '@/api/coupon'
import { ElMessage } from 'element-plus'
import { Timer } from '@element-plus/icons-vue'

const loading = ref(false)
const coupons = ref<any[]>([])
let timerInterval: any = null

function formatTime(time: string) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
}

function calcPercentage(c: any) {
  if (!c.total || c.total === 0) return 0
  const used = c.total - c.stock
  return Math.floor((used / c.total) * 100)
}

function getSeckillStatus(c: any) {
  const now = Date.now()
  const start = new Date(c.grabStartTime).getTime()
  const end = new Date(c.grabEndTime).getTime()
  
  if (now < start) return 'pending'
  if (now > end) return 'ended'
  if (c.stock <= 0) return 'empty'
  return 'active'
}

function getBtnText(c: any) {
  const status = getSeckillStatus(c)
  switch(status) {
    case 'pending': return '即将开始'
    case 'active': return '立即抢购'
    case 'empty': return '已抢光'
    case 'ended': return '已结束'
    default: return '不可用'
  }
}

function updateCountdowns() {
  const now = Date.now()
  coupons.value.forEach(c => {
    const start = new Date(c.grabStartTime).getTime()
    if (now < start) {
      const diff = start - now
      const h = Math.floor(diff / 3600000)
      const m = Math.floor((diff % 3600000) / 60000)
      const s = Math.floor((diff % 60000) / 1000)
      c.countdownStr = `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
    } else {
      c.countdownStr = ''
    }
  })
}

async function load() {
  loading.value = true
  try {
    const res = await getSeckillCoupons()
    coupons.value = res.data || []
    updateCountdowns()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleSeckill(c: any) {
  if (getSeckillStatus(c) !== 'active') return
  
  c.seckillLoading = true
  try {
    const res = await doSeckill(c.id)
    if (res.code === 200) {
      ElMessage.success(res.message || '抢购成功，正在发放中')
      // Refresh stock manually or just decrement locally
      c.stock--
    } else {
      ElMessage.warning(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e.message || '抢购失败')
  } finally {
    c.seckillLoading = false
  }
}

onMounted(() => {
  load()
  timerInterval = setInterval(updateCountdowns, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<style scoped lang="scss">
.seckill-page {
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
  padding-bottom: 40px;
}

.banner {
  background: linear-gradient(135deg, #ff4d4f 0%, #f5222d 100%);
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  
  .banner-content {
    text-align: center;
    h1 {
      font-size: 36px;
      font-weight: 800;
      margin-bottom: 10px;
      text-shadow: 0 2px 4px rgba(0,0,0,0.2);
    }
    p {
      font-size: 16px;
      opacity: 0.9;
    }
  }
}

.container {
  max-width: 1000px;
  margin: -40px auto 0;
  padding: 0 20px;
  position: relative;
  z-index: 10;
}

.loading-box, .empty-box {
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.seckill-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  }
  
  .left-section {
    width: 140px;
    height: 100px;
    background: linear-gradient(135deg, #fff1f0, #ffccc7);
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #cf1322;
    border: 1px solid #ffa39e;
    
    .amount {
      font-weight: 800;
      .symbol { font-size: 16px; }
      .num { font-size: 32px; }
    }
    .condition {
      font-size: 12px;
      margin-top: 4px;
      background: rgba(255,255,255,0.5);
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
  
  .middle-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .title {
      font-size: 18px;
      font-weight: 700;
      color: #1f293b;
    }
    
    .time-range {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #64748b;
    }
    
    .progress-box {
      margin-top: 8px;
      display: flex;
      align-items: center;
      gap: 12px;
      
      .el-progress {
        width: 200px;
      }
      .stock-text {
        font-size: 12px;
        color: #94a3b8;
      }
    }
  }
  
  .right-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    min-width: 120px;
    
    .countdown {
      text-align: center;
      p { font-size: 12px; color: #64748b; margin-bottom: 4px; }
      .timer {
        font-family: monospace;
        font-weight: 700;
        color: #cf1322;
        font-size: 16px;
      }
    }
    
    .el-button {
      width: 120px;
      background: linear-gradient(90deg, #ff4d4f, #f5222d);
      border: none;
      
      &:disabled {
        background: #f5f5f5;
        color: #d9d9d9;
      }
    }
  }
}
</style>
