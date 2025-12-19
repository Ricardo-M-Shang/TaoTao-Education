<template>
  <div class="teacher-stats-page" v-loading="loading">
    <div class="container">
      <div class="header">
        <div>
          <h2>收益统计</h2>
          <p class="sub">查看收入与订单数据</p>
        </div>
      </div>

      <div class="stat-cards">
        <div class="card">
          <p class="label">累计收益</p>
          <p class="value">¥{{ stats.totalIncome || 0 }}</p>
        </div>
        <div class="card">
          <p class="label">今日收益</p>
          <p class="value">¥{{ stats.todayIncome || 0 }}</p>
        </div>
        <div class="card">
          <p class="label">本月收益</p>
          <p class="value">¥{{ stats.monthIncome || 0 }}</p>
        </div>
        <div class="card">
          <p class="label">支付订单数</p>
          <p class="value">{{ stats.paidOrders || 0 }}</p>
        </div>
        <div class="card">
          <p class="label">学员人数</p>
          <p class="value">{{ stats.studentCount || 0 }}</p>
        </div>
      </div>
    </div>

    <!-- AI 助手悬浮按钮 -->
    <TeacherAIFloatButton />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getTeacherStats, TeacherStats } from '@/api/teacher'
import TeacherAIFloatButton from '@/components/TeacherAIFloatButton.vue'

const loading = ref(false)
const stats = ref<TeacherStats>({ totalIncome: 0, todayIncome: 0, monthIncome: 0, paidOrders: 0, studentCount: 0 })

async function load() {
  loading.value = true
  try {
    const res = await getTeacherStats()
    stats.value = res.data || stats.value
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.teacher-stats-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 960px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h2 { font-size: 18px; margin-bottom: 4px; }
  .sub { font-size: 12px; color: var(--text-muted); }
}
.stat-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
.card { padding: 14px; border: 1px solid #eee; border-radius: 10px; background: #fafafa;
  .label { font-size: 12px; color: var(--text-muted); margin-bottom: 6px; }
  .value { font-size: 20px; font-weight: 700; color: var(--text-primary); }
}
</style>

