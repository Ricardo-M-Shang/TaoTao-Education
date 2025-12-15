<template>
  <div class="study-center">
    <div class="container">
      <!-- 头部欢迎区域 -->
      <div class="welcome-section">
        <div class="welcome-content">
          <h1>学习中心</h1>
          <p>{{ getWelcomeMessage() }}</p>
          <div class="quick-stats">
            <div class="stat-item">
              <div class="stat-number">{{ userStats?.totalCourses || 0 }}</div>
              <div class="stat-label">学习课程</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ formatDuration(userStats?.totalStudyDuration || 0) }}</div>
              <div class="stat-label">学习时长</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ userStats?.currentStreak || 0 }}</div>
              <div class="stat-label">连续天数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ userLevel || 1 }}</div>
              <div class="stat-label">学习等级</div>
            </div>
          </div>
        </div>
        <div class="level-progress">
          <div class="level-info">
            <span>Lv.{{ userLevel || 1 }}</span>
            <span class="level-score">{{ userStats?.levelScore || 0 }}积分</span>
          </div>
          <el-progress 
            :percentage="getLevelProgress()" 
            :stroke-width="8"
            :show-text="false"
          />
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <div class="left-content">
          <!-- 正在学习的课程 -->
          <div class="section">
            <div class="section-header">
              <h3>正在学习</h3>
              <router-link to="/study/courses" class="more-link">查看全部</router-link>
            </div>
            <div class="learning-courses">
              <div v-if="learningCourses.length === 0" class="empty-state">
                <el-empty description="暂无正在学习的课程">
                  <el-button type="primary" @click="$router.push('/course')">去选课</el-button>
                </el-empty>
              </div>
              <div v-else class="course-grid">
                <div 
                  v-for="course in learningCourses.slice(0, 4)" 
                  :key="course.courseId" 
                  class="course-card"
                  @click="goToCourse(course.courseId)"
                >
                  <img :src="course.courseCover || defaultCover" alt="" />
                  <div class="course-info">
                    <h4>{{ course.courseTitle }}</h4>
                    <p>{{ course.teacherName }}</p>
                    <div class="progress-info">
                      <el-progress :percentage="course.completionRate || 0" />
                      <span>{{ course.completedLessons }}/{{ course.totalLessons }}课时</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 最近学习记录 -->
          <div class="section">
            <div class="section-header">
              <h3>最近学习</h3>
              <router-link to="/study/records" class="more-link">查看全部</router-link>
            </div>
            <div class="recent-studies">
              <div v-if="recentRecords.length === 0" class="empty-state">
                <el-empty description="暂无学习记录" />
              </div>
              <div v-else class="record-list">
                <div 
                  v-for="record in recentRecords" 
                  :key="record.id" 
                  class="record-item"
                >
                  <div class="record-info">
                    <h4>{{ record.courseTitle }}</h4>
                    <p>{{ record.lessonTitle }}</p>
                    <div class="record-meta">
                      <span>{{ formatDate(record.studyDate) }}</span>
                      <span>学习 {{ formatDuration(record.studyDuration) }}</span>
                      <el-tag v-if="record.isCompleted" type="success" size="small">已完成</el-tag>
                    </div>
                  </div>
                  <div class="record-progress">
                    <el-progress 
                      type="circle" 
                      :percentage="record.progressPercent" 
                      :width="60"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="right-content">
          <!-- 学习统计 -->
          <div class="section">
            <div class="section-header">
              <h3>学习统计</h3>
              <router-link to="/study/report" class="more-link">详细报告</router-link>
            </div>
            <div class="stats-cards">
              <div class="stats-card">
                <div class="stats-icon">📚</div>
                <div class="stats-info">
                  <div class="stats-number">{{ userStats?.completedCourses || 0 }}</div>
                  <div class="stats-label">已完成课程</div>
                </div>
              </div>
              <div class="stats-card">
                <div class="stats-icon">⏰</div>
                <div class="stats-info">
                  <div class="stats-number">{{ userStats?.totalStudyDays || 0 }}</div>
                  <div class="stats-label">学习天数</div>
                </div>
              </div>
              <div class="stats-card">
                <div class="stats-icon">🔥</div>
                <div class="stats-info">
                  <div class="stats-number">{{ userStats?.longestStreak || 0 }}</div>
                  <div class="stats-label">最长连续</div>
                </div>
              </div>
              <div class="stats-card">
                <div class="stats-icon">🎯</div>
                <div class="stats-info">
                  <div class="stats-number">{{ formatDuration(userStats?.avgDailyDuration || 0) }}</div>
                  <div class="stats-label">日均时长</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 学习建议 -->
          <div class="section">
            <div class="section-header">
              <h3>学习建议</h3>
            </div>
            <div class="advice-list">
              <div v-if="advice.length === 0" class="empty-state">
                <el-empty description="暂无建议" />
              </div>
              <div v-else>
                <div v-for="(item, index) in advice" :key="index" class="advice-item">
                  <el-icon class="advice-icon"><MagicStick /></el-icon>
                  <span>{{ item }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 完成率分布 -->
          <div class="section">
            <div class="section-header">
              <h3>完成率分布</h3>
            </div>
            <div class="completion-chart">
              <div v-for="item in completionDistribution" :key="item.rangeName" class="chart-item">
                <div class="chart-label">{{ item.rangeName }}</div>
                <div class="chart-bar">
                  <div 
                    class="chart-fill" 
                    :style="{ width: getDistributionWidth(item.count) + '%' }"
                  ></div>
                </div>
                <div class="chart-value">{{ item.count }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MagicStick } from '@element-plus/icons-vue'
import { 
  getUserLearningStats, 
  getUserLevel, 
  getLearningCourses, 
  getRecentStudyRecords,
  getLearningAdvice,
  getCompletionDistribution,
  type UserLearningStats,
  type LearningStats,
  type StudyRecord
} from '@/api/learning'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const userStats = ref<UserLearningStats | null>(null)
const userLevel = ref<number>(1)
const learningCourses = ref<LearningStats[]>([])
const recentRecords = ref<StudyRecord[]>([])
const advice = ref<string[]>([])
const completionDistribution = ref<Array<{ rangeName: string; count: number }>>([])

const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=300'

function getWelcomeMessage(): string {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息哦！'
  if (hour < 12) return '早上好！新的一天，从学习开始！'
  if (hour < 14) return '中午好！学习之余别忘了休息。'
  if (hour < 18) return '下午好！继续加油学习吧！'
  if (hour < 22) return '晚上好！今天学习了什么新知识？'
  return '夜深了，早点休息哦！'
}

function formatDuration(seconds: number): string {
  if (seconds < 3600) {
    return Math.floor(seconds / 60) + '分钟'
  }
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return hours + '小时' + (minutes > 0 ? minutes + '分钟' : '')
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date()
  yesterday.setDate(today.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    return date.toLocaleDateString()
  }
}

function getLevelProgress(): number {
  if (!userStats.value) return 0
  const score = userStats.value.levelScore
  const currentLevelMin = (userLevel.value - 1) * 1000
  const nextLevelMin = userLevel.value * 1000
  return ((score - currentLevelMin) / (nextLevelMin - currentLevelMin)) * 100
}

function getDistributionWidth(count: number): number {
  const maxCount = Math.max(...completionDistribution.value.map(item => item.count))
  return maxCount === 0 ? 0 : (count / maxCount) * 100
}

function goToCourse(courseId: number) {
  router.push(`/study/${courseId}`)
}

async function loadData() {
  loading.value = true
  try {
    const [
      userStatsRes,
      levelRes,
      learningCoursesRes,
      recentRecordsRes,
      adviceRes,
      distributionRes
    ] = await Promise.all([
      getUserLearningStats(),
      getUserLevel(),
      getLearningCourses(),
      getRecentStudyRecords(5),
      getLearningAdvice(),
      getCompletionDistribution()
    ])

    userStats.value = userStatsRes.data
    userLevel.value = levelRes.data
    learningCourses.value = learningCoursesRes.data || []
    recentRecords.value = recentRecordsRes.data || []
    advice.value = adviceRes.data || []
    completionDistribution.value = distributionRes.data || []
  } catch (error) {
    console.error('加载学习数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.study-center {
  background: var(--bg-color);
  min-height: calc(100vh - 90px);
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 30px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .welcome-content {
    h1 {
      font-size: 28px;
      margin-bottom: 10px;
      font-weight: 600;
    }

    p {
      font-size: 16px;
      margin-bottom: 25px;
      opacity: 0.9;
    }

    .quick-stats {
      display: flex;
      gap: 30px;

      .stat-item {
        text-align: center;

        .stat-number {
          font-size: 24px;
          font-weight: 700;
          margin-bottom: 5px;
        }

        .stat-label {
          font-size: 14px;
          opacity: 0.8;
        }
      }
    }
  }

  .level-progress {
    .level-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
      font-size: 14px;

      .level-score {
        opacity: 0.8;
      }
    }

    :deep(.el-progress-bar__outer) {
      background-color: rgba(255, 255, 255, 0.3);
    }

    :deep(.el-progress-bar__inner) {
      background: linear-gradient(90deg, #ffd700, #ffed4e);
    }
  }
}

.main-content {
  display: flex;
  gap: 30px;

  .left-content {
    flex: 2;
  }

  .right-content {
    flex: 1;
  }
}

.section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }

    .more-link {
      color: var(--primary-color);
      text-decoration: none;
      font-size: 14px;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  .course-card {
    display: flex;
    gap: 12px;
    padding: 12px;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: var(--primary-color);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    img {
      width: 80px;
      height: 60px;
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

      .progress-info {
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

.record-list {
  .record-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .record-info {
      flex: 1;

      h4 {
        font-size: 14px;
        margin-bottom: 4px;
      }

      p {
        font-size: 12px;
        color: #666;
        margin-bottom: 6px;
      }

      .record-meta {
        display: flex;
        gap: 12px;
        font-size: 12px;
        color: #999;
      }
    }
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;

  .stats-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #f8fafc;
    border-radius: 8px;

    .stats-icon {
      font-size: 24px;
    }

    .stats-info {
      .stats-number {
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }

      .stats-label {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

.advice-list {
  .advice-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 0;
    font-size: 14px;
    color: #666;

    .advice-icon {
      color: #f39c12;
    }
  }
}

.completion-chart {
  .chart-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .chart-label {
      width: 60px;
      font-size: 12px;
      color: #666;
    }

    .chart-bar {
      flex: 1;
      height: 20px;
      background: #f5f5f5;
      border-radius: 10px;
      overflow: hidden;

      .chart-fill {
        height: 100%;
        background: linear-gradient(90deg, var(--primary-color), #64b5f6);
        transition: width 0.3s;
      }
    }

    .chart-value {
      width: 30px;
      text-align: center;
      font-size: 12px;
      color: #666;
    }
  }
}

.empty-state {
  padding: 20px;
  text-align: center;
}
</style>
