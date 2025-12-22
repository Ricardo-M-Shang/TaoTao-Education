<template>
  <div class="ops-page">
    <div class="container" v-loading="loading">
      <div class="header">
        <div>
          <h2>运营中心</h2>
          <p class="sub">平台级订单、课程、用户概览</p>
        </div>
        <el-segmented v-model="range" :options="ranges" size="small" @change="loadTrend" />
      </div>

      <div class="cards">
        <div class="card clickable" @click="router.push('/ops/coupons')">
          <p class="label">营销管理</p>
          <p class="value" style="font-size: 18px; color: #333">优惠券管理</p>
          <p class="muted">发放 / 秒杀 / 统计</p>
        </div>
        <div class="card">
          <p class="label">平台总收入</p>
          <p class="value">¥ {{ orderOverview.totalIncome ?? 0 }}</p>
          <p class="muted">今日 ¥{{ orderOverview.todayIncome ?? 0 }} ｜ 本月 ¥{{ orderOverview.monthIncome ?? 0 }}</p>
        </div>
        <div class="card">
          <p class="label">已支付订单</p>
          <p class="value">{{ orderOverview.paidOrders ?? 0 }}</p>
          <p class="muted">支付订单（全部渠道）</p>
        </div>
        <div class="card">
          <p class="label">课程总数</p>
          <p class="value">{{ courseOverview.total ?? 0 }}</p>
          <p class="muted">待审 {{ courseOverview.pending ?? 0 }} ｜ 上架 {{ courseOverview.published ?? 0 }} ｜ 下架 {{ courseOverview.offline ?? 0 }}</p>
        </div>
        <div class="card">
          <p class="label">用户概况</p>
          <p class="value">{{ userOverview.total ?? 0 }}</p>
          <p class="muted">学员 {{ userOverview.students ?? 0 }} ｜ 讲师 {{ userOverview.teachers ?? 0 }} ｜ 机构 {{ userOverview.orgs ?? 0 }}</p>
        </div>
      </div>

      <div class="chart-card" ref="trendRef"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getOpsCourseOverview, getOpsOrderOverview, getOpsOrderTrend, getOpsUserOverview } from '@/api/ops'

const router = useRouter()
const loading = ref(false)
const orderOverview = reactive<{ totalIncome?: number; todayIncome?: number; monthIncome?: number; paidOrders?: number }>({})
const courseOverview = reactive<{ total?: number; pending?: number; published?: number; offline?: number }>({})
const userOverview = reactive<{ total?: number; students?: number; teachers?: number; orgs?: number; ops?: number }>({})

const range = ref<'7' | '30'>('7')
const ranges = [{ label: '近7天', value: '7' }, { label: '近30天', value: '30' }]

const trendRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

async function loadOverview() {
  loading.value = true
  try {
    const [orderRes, courseRes, userRes] = await Promise.all([
      getOpsOrderOverview(),
      getOpsCourseOverview(),
      getOpsUserOverview()
    ])
    Object.assign(orderOverview, orderRes.data || {})
    Object.assign(courseOverview, courseRes.data || {})
    Object.assign(userOverview, userRes.data || {})
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function loadTrend() {
  try {
    const res = await getOpsOrderTrend({ days: Number(range.value) })
    const list = res.data || []
    await nextTick()
    if (!trendRef.value) return
    if (!chart) {
      chart = echarts.init(trendRef.value)
    }
    const days = list.map((i: any) => i.day)
    const income = list.map((i: any) => Number(i.income || 0))
    const orders = list.map((i: any) => Number(i.paidOrders || 0))
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['收入', '订单数'], top: 6 },
      grid: { left: '3%', right: '4%', bottom: '8%', containLabel: true },
      xAxis: { type: 'category', data: days, axisLabel: { color: '#9ca3af' } },
      yAxis: [
        { type: 'value', name: '收入', axisLabel: { color: '#9ca3af' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
        { type: 'value', name: '订单', axisLabel: { color: '#9ca3af' }, splitLine: { show: false } }
      ],
      series: [
        { name: '收入', type: 'line', smooth: true, data: income, yAxisIndex: 0, itemStyle: { color: '#6366f1' } },
        { name: '订单数', type: 'bar', data: orders, yAxisIndex: 1, barWidth: '40%', itemStyle: { color: '#34d399' } }
      ]
    })
    chart.resize()
  } catch (error) {
    console.error(error)
  }
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  loadOverview()
  loadTrend()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped lang="scss">
.ops-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 1000px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; box-shadow: 0 10px 28px rgba(99,102,241,0.05); }
.header { margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center;
  h2 { margin: 0; }
  .sub { color: var(--text-muted); font-size: 12px; margin: 4px 0 0; }
}
.cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px,1fr)); gap: 12px; }
.card { padding: 16px; border: 1px solid #f0f0f0; border-radius: 12px; background: linear-gradient(135deg,#eef2ff,#ffffff);
  .label { margin: 0 0 6px 0; color: var(--text-muted); font-size: 12px; }
  .value { margin: 0 0 4px 0; font-size: 22px; font-weight: 700; color: var(--primary-color); }
  .muted { margin: 0; color: #9ca3af; font-size: 12px; }
}
.chart-card { margin-top: 14px; height: 340px; border: 1px solid #f0f0f0; border-radius: 12px; padding: 10px; }
</style>


