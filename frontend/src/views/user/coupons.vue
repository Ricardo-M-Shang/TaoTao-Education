<template>
  <div class="coupon-page" v-loading="loading">
    <div class="container">
      <div class="header">
        <h2>我的优惠券</h2>
        <div class="actions">
          <el-button type="primary" plain @click="goUse">去选课</el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="未使用" name="unused" />
        <el-tab-pane label="已使用" name="used" />
        <el-tab-pane label="已过期" name="expired" />
      </el-tabs>

      <div class="list">
        <el-empty v-if="!coupons.length" description="暂无优惠券" />
        <div v-else class="coupon-grid">
          <div v-for="c in coupons" :key="c.userCouponId || c.id" class="coupon-card">
            <div class="left">
              <div class="price" v-if="c.type === 1">¥<span>{{ c.discountAmount || 0 }}</span></div>
              <div class="price" v-else>{{ ((c.discountRate || 1) * 100).toFixed(0) }}<span>折</span></div>
              <div class="limit">满 ¥{{ c.thresholdAmount || 0 }} 可用</div>
            </div>
            <div class="right">
              <div class="title">{{ c.name || (c.type === 1 ? '满减券' : '折扣券') }}</div>
              <div class="time">有效期：{{ formatTime(c.validFrom) }} ~ {{ formatTime(c.validTo) }}</div>
              <div class="status">
                <el-tag v-if="c.userStatus === 1" type="info" size="small">已使用</el-tag>
                <el-tag v-else-if="isExpired(c)" type="warning" size="small">已过期</el-tag>
                <el-tag v-else type="success" size="small">未使用</el-tag>
              </div>
              <div class="btns" v-if="c.userStatus === 0 && !isExpired(c)">
                <el-button size="small" type="primary" @click="goUse">立即使用</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="pager" v-if="total > pageSize">
        <el-pagination
          layout="prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyCoupons } from '@/api/coupon'

const router = useRouter()
const loading = ref(false)
const coupons = ref<any[]>([])
const activeTab = ref<'unused' | 'used' | 'expired'>('unused')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

function statusValue() {
  if (activeTab.value === 'used') return 1
  if (activeTab.value === 'expired') return 2
  return 0
}

function isExpired(c: any) {
  return c.validTo && new Date(c.validTo).getTime() < Date.now()
}

function formatTime(time?: string) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
}

async function load() {
  loading.value = true
  try {
    const res = await getMyCoupons({ status: statusValue(), pageNum: pageNum.value, pageSize: pageSize.value })
    coupons.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  pageNum.value = 1
  load()
}

function handlePageChange(p: number) {
  pageNum.value = p
  load()
}

function goUse() {
  router.push('/course')
}

onMounted(load)
</script>

<style scoped lang="scss">
.coupon-page {
  background: var(--bg-color);
  min-height: calc(100vh - 90px);
  padding: 20px 0;
}
.container {
  max-width: 960px;
  margin: 0 auto;
  background: #fff;
  padding: 16px;
  border-radius: 12px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.list {
  margin-top: 10px;
}
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
.coupon-card {
  display: flex;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}
.left {
  width: 110px;
  background: linear-gradient(135deg, #ff9f43, #ff6b00);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
}
.left .price {
  font-size: 18px;
  font-weight: 700;
}
.left .price span {
  font-size: 28px;
}
.left .limit {
  font-size: 12px;
  margin-top: 6px;
  opacity: 0.9;
}
.right {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.title {
  font-weight: 600;
  font-size: 15px;
}
.time {
  font-size: 12px;
  color: #666;
}
.status {
  margin-top: 2px;
}
.btns {
  margin-top: 6px;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}
</style>

