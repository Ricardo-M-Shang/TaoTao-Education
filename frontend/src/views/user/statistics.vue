<template>
  <div class="statistics-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>学习统计</h2>
        <p class="subtitle">了解你的学习情况，持续进步</p>
      </div>

      <!-- 统计卡片 -->
      <div class="stat-cards" v-loading="loading">
        <div class="stat-card">
          <div class="stat-icon study-time">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatStudyTime(statistics.totalStudyTime) }}</span>
            <span class="stat-label">累计学习时长</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon courses">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.totalCourses }}</span>
            <span class="stat-label">学习课程数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon finished">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.finishedCourses }}</span>
            <span class="stat-label">已完成课程</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon streak">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.studyDays }}</span>
            <span class="stat-label">学习天数</span>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <!-- 最近7天学习时长 -->
        <div class="chart-card">
          <h3>最近7天学习时长</h3>
          <div class="chart-container" ref="weeklyChartRef"></div>
        </div>

        <!-- 课程进度分布 -->
        <div class="chart-card">
          <h3>课程进度分布</h3>
          <div class="chart-container" ref="progressChartRef"></div>
        </div>
      </div>

      <!-- 最近学习 -->
      <div class="recent-section">
        <h3>最近学习</h3>
        <div class="recent-list" v-loading="loadingRecent">
          <el-empty v-if="!loadingRecent && !recentRecords.length" description="暂无学习记录">
            <el-button type="primary" size="small" @click="$router.push('/course')">去选课</el-button>
          </el-empty>
          <div v-for="record in recentRecords" :key="record.id" class="recent-item" @click="goStudy(record)">
            <img :src="record.courseCover || defaultCover" />
            <div class="recent-info">
              <h4>{{ record.courseTitle }}</h4>
              <p>{{ record.lessonTitle || '开始学习' }}</p>
              <div class="progress-bar">
                <el-progress :percentage="record.progress" :show-text="false" :stroke-width="4" />
                <span>{{ record.progress }}%</span>
              </div>
            </div>
            <div class="recent-time">
              {{ formatRelativeTime(record.updateTime) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 学习成就 -->
      <div class="achievement-section">
        <h3>学习成就</h3>
        <div class="achievement-list">
          <div :class="['achievement-item', { unlocked: statistics.totalCourses >= 1 }]">
            <div class="achievement-icon">🎯</div>
            <span class="achievement-name">初学者</span>
            <span class="achievement-desc">学习第一门课程</span>
          </div>
          <div :class="['achievement-item', { unlocked: statistics.finishedCourses >= 1 }]">
            <div class="achievement-icon">🏆</div>
            <span class="achievement-name">完成者</span>
            <span class="achievement-desc">完成一门课程</span>
          </div>
          <div :class="['achievement-item', { unlocked: statistics.totalStudyTime >= 600 }]">
            <div class="achievement-icon">⏰</div>
            <span class="achievement-name">坚持学习</span>
            <span class="achievement-desc">累计学习10小时</span>
          </div>
          <div :class="['achievement-item', { unlocked: statistics.studyDays >= 7 }]">
            <div class="achievement-icon">🔥</div>
            <span class="achievement-name">连续学习</span>
            <span class="achievement-desc">学习7天</span>
          </div>
          <div :class="['achievement-item', { unlocked: statistics.finishedCourses >= 5 }]">
            <div class="achievement-icon">⭐</div>
            <span class="achievement-name">学习达人</span>
            <span class="achievement-desc">完成5门课程</span>
          </div>
          <div :class="['achievement-item', { unlocked: statistics.totalStudyTime >= 3600 }]">
            <div class="achievement-icon">💎</div>
            <span class="achievement-name">学习专家</span>
            <span class="achievement-desc">累计学习60小时</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Timer, Reading, Trophy, Calendar } from '@element-plus/icons-vue'
import { getStudyStatistics, getRecentStudyRecords, StudyStatistics, RecentStudyRecord } from '@/api/study'
import * as echarts from 'echarts'

const router = useRouter()
const loading = ref(false)
const loadingRecent = ref(false)
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'

const statistics = ref<StudyStatistics>({
  totalStudyTime: 0,
  totalCourses: 0,
  finishedCourses: 0,
  studyDays: 0,
  weeklyStudyTime: []
})

const recentRecords = ref<RecentStudyRecord[]>([])

const weeklyChartRef = ref<HTMLElement | null>(null)
const progressChartRef = ref<HTMLElement | null>(null)
let weeklyChart: echarts.ECharts | null = null
let progressChart: echarts.ECharts | null = null

function formatStudyTime(minutes: number): string {
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours < 24) return mins > 0 ? `${hours}小时${mins}分` : `${hours}小时`
  const days = Math.floor(hours / 24)
  const remainHours = hours % 24
  return remainHours > 0 ? `${days}天${remainHours}小时` : `${days}天`
}

function formatRelativeTime(time: string): string {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))}分钟前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  if (diff < 7 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function goStudy(record: RecentStudyRecord) {
  router.push(`/study/${record.courseId}?lesson=${record.lessonId}`)
}

function initWeeklyChart() {
  if (!weeklyChartRef.value) return
  weeklyChart = echarts.init(weeklyChartRef.value)
  
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const today = new Date().getDay()
  const sortedDays = [...days.slice(today), ...days.slice(0, today)]
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}分钟'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedDays,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#9ca3af', fontSize: 10 }
    },
    series: [{
      data: statistics.value.weeklyStudyTime || [0, 0, 0, 0, 0, 0, 0],
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#818cf8' },
          { offset: 1, color: '#6366f1' }
        ])
      }
    }]
  }
  weeklyChart.setOption(option)
}

function initProgressChart() {
  if (!progressChartRef.value) return
  progressChart = echarts.init(progressChartRef.value)
  
  const total = statistics.value.totalCourses || 0
  const finished = statistics.value.finishedCourses || 0
  const learning = total - finished
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}门 ({d}%)'
    },
    legend: {
      bottom: '5%',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 11, color: '#6b7280' }
    },
    series: [{
      name: '课程进度',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: [
        { value: finished, name: '已完成', itemStyle: { color: '#10b981' } },
        { value: learning, name: '学习中', itemStyle: { color: '#6366f1' } }
      ]
    }]
  }
  progressChart.setOption(option)
}

function handleResize() {
  weeklyChart?.resize()
  progressChart?.resize()
}

async function loadStatistics() {
  loading.value = true
  try {
    const res = await getStudyStatistics()
    statistics.value = res.data
    nextTick(() => {
      initWeeklyChart()
      initProgressChart()
    })
  } catch (e) {
    console.error('加载统计数据失败', e)
  } finally {
    loading.value = false
  }
}

async function loadRecentRecords() {
  loadingRecent.value = true
  try {
    const res = await getRecentStudyRecords()
    recentRecords.value = res.data || []
  } catch (e) {
    console.error('加载最近学习记录失败', e)
  } finally {
    loadingRecent.value = false
  }
}

onMounted(() => {
  loadStatistics()
  loadRecentRecords()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  weeklyChart?.dispose()
  progressChart?.dispose()
})
</script>

<style lang="scss" scoped>
.statistics-page {
  background: var(--bg-color);
  min-height: calc(100vh - 90px);
  padding: 20px 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  margin-bottom: 24px;
  
  h2 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  
  .subtitle {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: #fff;
    
    &.study-time { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
    &.courses { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
    &.finished { background: linear-gradient(135deg, #10b981, #34d399); }
    &.streak { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
  }
  
  .stat-info {
    display: flex;
    flex-direction: column;
    
    .stat-value {
      font-size: 22px;
      font-weight: 700;
      color: var(--text-primary);
    }
    
    .stat-label {
      font-size: 12px;
      color: var(--text-muted);
      margin-top: 2px;
    }
  }
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  
  h3 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 16px;
  }
  
  .chart-container {
    height: 220px;
  }
}

.recent-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  
  h3 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 16px;
  }
}

.recent-list {
  .recent-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px;
    border-radius: 10px;
    margin-bottom: 10px;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #f0f0f0;
    
    &:last-child { margin-bottom: 0; }
    
    &:hover {
      background: #f8fafc;
      border-color: var(--primary-color);
    }
    
    img {
      width: 80px;
      height: 50px;
      object-fit: cover;
      border-radius: 6px;
    }
    
    .recent-info {
      flex: 1;
      
      h4 {
        font-size: 13px;
        font-weight: 500;
        margin-bottom: 4px;
      }
      
      p {
        font-size: 11px;
        color: var(--text-muted);
        margin-bottom: 6px;
      }
      
      .progress-bar {
        display: flex;
        align-items: center;
        gap: 10px;
        
        :deep(.el-progress) {
          flex: 1;
          max-width: 200px;
        }
        
        span {
          font-size: 11px;
          font-weight: 500;
          color: var(--primary-color);
        }
      }
    }
    
    .recent-time {
      font-size: 11px;
      color: var(--text-muted);
    }
  }
}

.achievement-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  
  h3 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 16px;
  }
}

.achievement-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.achievement-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 14px;
  border-radius: 12px;
  background: #f8fafc;
  text-align: center;
  opacity: 0.5;
  filter: grayscale(1);
  transition: all 0.3s;
  
  &.unlocked {
    opacity: 1;
    filter: none;
    background: linear-gradient(135deg, #fef3c7, #fde68a);
    
    .achievement-icon {
      transform: scale(1.1);
    }
  }
  
  .achievement-icon {
    font-size: 32px;
    margin-bottom: 8px;
    transition: transform 0.3s;
  }
  
  .achievement-name {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  
  .achievement-desc {
    font-size: 10px;
    color: var(--text-muted);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .achievement-list {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

