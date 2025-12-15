<template>
  <div class="org-stats-page">
    <div class="container" v-loading="loading">
      <div class="header">
        <div>
          <h2>机构收益统计</h2>
          <p class="sub">收入与订单概览</p>
        </div>
      </div>

      <div class="cards">
        <div class="card">
          <p class="label">总收入</p>
          <p class="value">¥ {{ stats.totalIncome || 0 }}</p>
        </div>
        <div class="card">
          <p class="label">今日收入</p>
          <p class="value">¥ {{ stats.todayIncome || 0 }}</p>
        </div>
        <div class="card">
          <p class="label">本月收入</p>
          <p class="value">¥ {{ stats.monthIncome || 0 }}</p>
        </div>
        <div class="card">
          <p class="label">已支付订单</p>
          <p class="value">{{ stats.paidOrders || 0 }}</p>
        </div>
      </div>

      <div class="toolbar">
        <el-segmented v-model="range" :options="ranges" size="small" @change="loadTrend" />
      </div>

      <div class="chart-card" ref="trendRef"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOrgStats, getOrgTrend } from '@/api/org'

const loading = ref(false)
const stats = reactive<{ totalIncome?: number; todayIncome?: number; monthIncome?: number; paidOrders?: number }>({})
const range = ref<'7' | '30'>('7')
const ranges = [{ label: '近7天', value: '7' }, { label: '近30天', value: '30' }]
const trendRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

async function load() {
  loading.value = true
  try {
    const res = await getOrgStats()
    Object.assign(stats, res.data || {})
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadTrend() {
  try {
    const res = await getOrgTrend({ days: Number(range.value) })
    const list = res.data || []
    await nextTick()
    if (!trendRef.value) return
    if (!chart) chart = echarts.init(trendRef.value)
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
  } catch (e) { console.error(e) }
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  load()
  loadTrend()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped lang="scss">
.org-stats-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 900px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { margin-bottom: 14px;
  h2 { margin: 0; }
  .sub { color: var(--text-muted); font-size: 12px; }
}
.cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px,1fr)); gap: 12px; }
.card { padding: 16px; border: 1px solid #eee; border-radius: 10px; background: linear-gradient(135deg, #eef2ff, #ffffff); box-shadow: 0 10px 24px rgba(99,102,241,0.08);
  .label { margin: 0 0 6px 0; color: var(--text-muted); font-size: 12px; }
  .value { margin: 0; font-size: 20px; font-weight: 700; color: var(--primary-color); }
}
.toolbar { margin: 14px 0; display: flex; justify-content: flex-end; }
.chart-card { height: 320px; border: 1px solid #f0f0f0; border-radius: 10px; padding: 10px; }
</style>


