<template>
  <div class="study-report">
    <div class="container">
      <div class="header">
        <h2>学习报告</h2>
        <div class="period-tabs">
          <el-radio-group v-model="selectedPeriod" @change="onPeriodChange">
            <el-radio-button value="week">本周</el-radio-button>
            <el-radio-button value="month">本月</el-radio-button>
            <el-radio-button value="year">本年</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="report-content" v-loading="loading">
        <!-- 概览统计 -->
        <div class="overview-section">
          <div class="overview-cards">
            <div class="overview-card">
              <div class="card-icon">📚</div>
              <div class="card-content">
                <div class="card-number">{{ report?.studyCourseCount || 0 }}</div>
                <div class="card-label">学习课程</div>
              </div>
            </div>
            <div class="overview-card">
              <div class="card-icon">⏰</div>
              <div class="card-content">
                <div class="card-number">{{ formatDuration(report?.totalStudyDuration || 0) }}</div>
                <div class="card-label">学习时长</div>
              </div>
            </div>
            <div class="overview-card">
              <div class="card-icon">📅</div>
              <div class="card-content">
                <div class="card-number">{{ report?.totalStudyDays || 0 }}</div>
                <div class="card-label">学习天数</div>
              </div>
            </div>
            <div class="overview-card">
              <div class="card-icon">🎯</div>
              <div class="card-content">
                <div class="card-number">{{ formatDuration(report?.avgDailyDuration || 0) }}</div>
                <div class="card-label">日均时长</div>
              </div>
            </div>
            <div class="overview-card">
              <div class="card-icon">✅</div>
              <div class="card-content">
                <div class="card-number">{{ report?.completedCourseCount || 0 }}</div>
                <div class="card-label">完成课程</div>
              </div>
            </div>
            <div class="overview-card">
              <div class="card-icon">🔥</div>
              <div class="card-content">
                <div class="card-number">{{ report?.currentStreak || 0 }}</div>
                <div class="card-label">连续天数</div>
              </div>
            </div>
          </div>
        </div>

        <div class="charts-section">
          <!-- 学习时长趋势 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>学习时长趋势</h3>
              <span class="chart-subtitle">{{ getPeriodText() }}学习时长变化</span>
            </div>
            <div class="chart-container">
              <div ref="durationTrendChart" class="chart"></div>
            </div>
          </div>

          <!-- 完成率分布 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>课程完成率分布</h3>
              <span class="chart-subtitle">不同完成率课程数量</span>
            </div>
            <div class="chart-container">
              <div ref="completionChart" class="chart"></div>
            </div>
          </div>

          <!-- 学习时段分布 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>学习时段偏好</h3>
              <span class="chart-subtitle">各时段学习时长分布</span>
            </div>
            <div class="chart-container">
              <div ref="timeDistributionChart" class="chart"></div>
            </div>
          </div>
        </div>

        <div class="details-section">
          <!-- 最近学习的课程 -->
          <div class="detail-card">
            <div class="detail-header">
              <h3>最近学习</h3>
            </div>
            <div class="course-list">
              <div v-if="!report?.recentCourses?.length" class="empty">
                <el-empty description="暂无学习记录" />
              </div>
              <div v-else>
                <div 
                  v-for="course in report.recentCourses" 
                  :key="course.courseId" 
                  class="course-item"
                  @click="goToCourse(course.courseId)"
                >
                  <img :src="course.courseCover || defaultCover" alt="" />
                  <div class="course-info">
                    <h4>{{ course.courseTitle }}</h4>
                    <p>{{ course.teacherName }}</p>
                    <div class="course-progress">
                      <el-progress :percentage="course.completionRate || 0" />
                      <span>{{ formatDateTime(course.lastStudyTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 学习建议 -->
          <div class="detail-card">
            <div class="detail-header">
              <h3>学习建议</h3>
            </div>
            <div class="advice-list">
              <div v-if="!report?.suggestions?.length" class="empty">
                <el-empty description="暂无建议" />
              </div>
              <div v-else>
                <div v-for="(advice, index) in report.suggestions" :key="index" class="advice-item">
                  <el-icon class="advice-icon"><MagicStick /></el-icon>
                  <span>{{ advice }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 新完成的课程 -->
        <div v-if="report?.newCompletedCourses?.length" class="achievements-section">
          <div class="achievements-header">
            <h3>🎉 本{{ getPeriodText() }}新完成的课程</h3>
          </div>
          <div class="achievements-list">
            <div 
              v-for="course in report.newCompletedCourses" 
              :key="course.courseId" 
              class="achievement-item"
            >
              <img :src="course.courseCover || defaultCover" alt="" />
              <div class="achievement-info">
                <h4>{{ course.courseTitle }}</h4>
                <p>{{ course.teacherName }}</p>
                <div class="achievement-stats">
                  <span>学习时长: {{ formatDuration(course.studyDuration) }}</span>
                  <span>学习天数: {{ course.studyDays }}天</span>
                </div>
              </div>
              <div class="achievement-badge">
                <el-icon><Trophy /></el-icon>
                <span>已完成</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { MagicStick, Trophy } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getLearningReport, type LearningReport } from '@/api/learning'

const router = useRouter()

const loading = ref(false)
const selectedPeriod = ref<'week' | 'month' | 'year'>('week')
const report = ref<LearningReport | null>(null)

const durationTrendChart = ref<HTMLElement>()
const completionChart = ref<HTMLElement>()
const timeDistributionChart = ref<HTMLElement>()

const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'

function formatDuration(seconds: number): string {
  if (seconds < 3600) {
    return Math.floor(seconds / 60) + '分钟'
  }
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return hours + '小时' + (minutes > 0 ? minutes + '分钟' : '')
}

function formatDateTime(dateTimeStr: string | undefined): string {
  if (!dateTimeStr) return ''
  return new Date(dateTimeStr).toLocaleDateString('zh-CN', {
    month: 'numeric',
    day: 'numeric'
  })
}

function getPeriodText(): string {
  const periodMap = {
    week: '周',
    month: '月',
    year: '年'
  }
  return periodMap[selectedPeriod.value]
}

function goToCourse(courseId: number) {
  router.push(`/study/${courseId}`)
}

function onPeriodChange() {
  loadReport()
}

async function loadReport() {
  loading.value = true
  try {
    const res = await getLearningReport({
      period: selectedPeriod.value,
      type: 'detail'
    })
    report.value = res.data
    
    // 等待DOM更新后渲染图表
    await nextTick()
    renderCharts()
  } catch (error) {
    console.error('加载学习报告失败:', error)
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  renderDurationTrend()
  renderCompletionDistribution()
  renderTimeDistribution()
}

function renderDurationTrend() {
  if (!durationTrendChart.value || !report.value?.durationTrend) return

  const chart = echarts.init(durationTrendChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        return `${data.name}<br/>学习时长: ${formatDuration(data.value)}`
      }
    },
    xAxis: {
      type: 'category',
      data: report.value.durationTrend.map(item => 
        new Date(item.studyDate).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
      ),
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => formatDuration(value)
      }
    },
    series: [{
      type: 'line',
      data: report.value.durationTrend.map(item => item.duration),
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 181, 246, 0.3)' },
          { offset: 1, color: 'rgba(64, 181, 246, 0.05)' }
        ])
      },
      itemStyle: { color: '#40B5F6' },
      lineStyle: { color: '#40B5F6', width: 3 }
    }],
    grid: { left: 60, right: 20, top: 20, bottom: 40 }
  }
  chart.setOption(option)
}

function renderCompletionDistribution() {
  if (!completionChart.value || !report.value?.completionDistribution) return

  const chart = echarts.init(completionChart.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}门课程 ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: report.value.completionDistribution.map(item => ({
        name: item.rangeName,
        value: item.count
      })),
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{c}门'
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  chart.setOption(option)
}

function renderTimeDistribution() {
  if (!timeDistributionChart.value || !report.value?.timeDistribution) return

  const chart = echarts.init(timeDistributionChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        return `${data.name}<br/>学习时长: ${formatDuration(data.value)}`
      }
    },
    xAxis: {
      type: 'category',
      data: report.value.timeDistribution.map(item => item.name),
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => formatDuration(value)
      }
    },
    series: [{
      type: 'bar',
      data: report.value.timeDistribution.map(item => item.duration),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }],
    grid: { left: 60, right: 20, top: 20, bottom: 40 }
  }
  chart.setOption(option)
}

onMounted(() => {
  loadReport()
})
</script>

<style lang="scss" scoped>
.study-report {
  background: var(--bg-color);
  min-height: calc(100vh - 90px);
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;

  h2 {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.overview-section {
  margin-bottom: 30px;

  .overview-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 20px;

    .overview-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      transition: all 0.2s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      }

      .card-icon {
        font-size: 32px;
      }

      .card-content {
        .card-number {
          font-size: 20px;
          font-weight: 700;
          color: #333;
          margin-bottom: 4px;
        }

        .card-label {
          font-size: 14px;
          color: #666;
        }
      }
    }
  }
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;

  .chart-card {
    background: white;
    border-radius: 12px;
    padding: 20px;

    .chart-header {
      margin-bottom: 20px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 4px 0;
      }

      .chart-subtitle {
        font-size: 12px;
        color: #666;
      }
    }

    .chart-container {
      .chart {
        width: 100%;
        height: 300px;
      }
    }
  }
}

.details-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 30px;

  .detail-card {
    background: white;
    border-radius: 12px;
    padding: 20px;

    .detail-header {
      margin-bottom: 20px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        margin: 0;
      }
    }

    .course-list {
      .course-item {
        display: flex;
        gap: 12px;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;
        margin-bottom: 12px;

        &:hover {
          background: #f8fafc;
        }

        img {
          width: 60px;
          height: 45px;
          object-fit: cover;
          border-radius: 6px;
        }

        .course-info {
          flex: 1;

          h4 {
            font-size: 14px;
            margin-bottom: 4px;
            color: #333;
          }

          p {
            font-size: 12px;
            color: #666;
            margin-bottom: 8px;
          }

          .course-progress {
            display: flex;
            flex-direction: column;
            gap: 4px;

            span {
              font-size: 12px;
              color: #999;
            }
          }
        }
      }
    }

    .advice-list {
      .advice-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 12px 0;
        font-size: 14px;
        color: #666;

        .advice-icon {
          color: #f39c12;
          flex-shrink: 0;
        }
      }
    }
  }
}

.achievements-section {
  background: white;
  border-radius: 12px;
  padding: 20px;

  .achievements-header {
    margin-bottom: 20px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
      color: #333;
    }
  }

  .achievements-list {
    .achievement-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: linear-gradient(135deg, #667eea10, #764ba210);
      border-radius: 8px;
      margin-bottom: 12px;

      img {
        width: 80px;
        height: 60px;
        object-fit: cover;
        border-radius: 8px;
      }

      .achievement-info {
        flex: 1;

        h4 {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 4px;
          color: #333;
        }

        p {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;
        }

        .achievement-stats {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: #999;
        }
      }

      .achievement-badge {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        color: #f39c12;

        .el-icon {
          font-size: 24px;
        }

        span {
          font-size: 12px;
          font-weight: 600;
        }
      }
    }
  }
}

.empty {
  padding: 40px;
  text-align: center;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 16px;
  }

  .details-section {
    grid-template-columns: 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }
}
</style>
