<template>
  <div class="courses-page">
    <div class="container">
      <div class="page-header">
        <h2>我的课程</h2>
        <div class="stats" v-if="courses.length">
          <div class="stat-item">
            <span class="num">{{ courses.length }}</span>
            <span class="label">总课程</span>
          </div>
          <div class="stat-item">
            <span class="num">{{ finishedCount }}</span>
            <span class="label">已完成</span>
          </div>
          <div class="stat-item">
            <span class="num">{{ learningCount }}</span>
            <span class="label">学习中</span>
          </div>
        </div>
      </div>

      <div class="filter-bar" v-if="courses.length">
        <el-radio-group v-model="filter" size="small" @change="filterCourses">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="learning">学习中</el-radio-button>
          <el-radio-button value="finished">已完成</el-radio-button>
        </el-radio-group>
        <el-select v-model="sortBy" size="small" placeholder="排序" style="width: 120px" @change="sortCourses">
          <el-option label="最近学习" value="recent" />
          <el-option label="购买时间" value="buyTime" />
          <el-option label="学习进度" value="progress" />
        </el-select>
      </div>

      <div class="list" v-loading="loading">
        <el-empty v-if="!loading && !filteredCourses.length" :description="emptyText">
          <el-button v-if="filter === 'all'" type="primary" size="small" @click="$router.push('/course')">浏览课程</el-button>
        </el-empty>

        <div v-for="c in filteredCourses" :key="c.id" class="item">
          <div class="course-cover" @click="$router.push(`/course/${c.courseId}`)">
            <img :src="c.courseCover || defaultCover" />
            <div class="play-overlay">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <span v-if="c.isFinished" class="finished-badge">
              <el-icon><CircleCheck /></el-icon>
            </span>
          </div>
          <div class="info">
            <h4 @click="$router.push(`/course/${c.courseId}`)">{{ c.courseTitle }}</h4>
            <p class="teacher">
              <el-icon><User /></el-icon>
              {{ c.teacherName }}
            </p>
            <div class="progress-section">
              <div class="progress-header">
                <span class="progress-label">
                  <template v-if="c.isFinished">
                    <el-icon class="icon-success"><CircleCheck /></el-icon>
                    学习完成
                  </template>
                  <template v-else>
                    学习进度
                  </template>
                </span>
                <span class="progress-value" :class="{ finished: c.isFinished }">{{ c.progress || 0 }}%</span>
              </div>
              <el-progress 
                :percentage="c.progress || 0" 
                :stroke-width="8" 
                :status="c.isFinished ? 'success' : ''"
                :show-text="false"
              />
            </div>
            <div class="meta">
              <span v-if="c.lastStudyTime" class="last-study">
                <el-icon><Clock /></el-icon>
                {{ formatTime(c.lastStudyTime) }} 学习
              </span>
              <span v-else class="not-started">
                <el-icon><Calendar /></el-icon>
                尚未开始学习
              </span>
            </div>
          </div>
          <div class="actions">
            <el-button type="primary" round @click="goStudy(c)">
              <el-icon><VideoPlay /></el-icon>
              {{ getStudyButtonText(c) }}
            </el-button>
            <el-dropdown trigger="click" @command="handleCommand($event, c)">
              <el-button round>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="detail">课程详情</el-dropdown-item>
                  <el-dropdown-item command="review">评价课程</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { VideoPlay, CircleCheck, Clock, Calendar, User, MoreFilled } from '@element-plus/icons-vue'
import { getUserCourses, UserCourseInfo } from '@/api/userCourse'

const router = useRouter()
const loading = ref(false)
const courses = ref<UserCourseInfo[]>([])
const filter = ref('all')
const sortBy = ref('recent')
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'

const finishedCount = computed(() => courses.value.filter(c => c.isFinished).length)
const learningCount = computed(() => courses.value.filter(c => !c.isFinished && c.progress > 0).length)

const filteredCourses = computed(() => {
  let result = [...courses.value]
  
  // 筛选
  if (filter.value === 'learning') {
    result = result.filter(c => !c.isFinished)
  } else if (filter.value === 'finished') {
    result = result.filter(c => c.isFinished)
  }
  
  // 排序
  if (sortBy.value === 'recent') {
    result.sort((a, b) => {
      const timeA = a.lastStudyTime ? new Date(a.lastStudyTime).getTime() : 0
      const timeB = b.lastStudyTime ? new Date(b.lastStudyTime).getTime() : 0
      return timeB - timeA
    })
  } else if (sortBy.value === 'buyTime') {
    result.sort((a, b) => {
      const timeA = new Date(a.createTime).getTime()
      const timeB = new Date(b.createTime).getTime()
      return timeB - timeA
    })
  } else if (sortBy.value === 'progress') {
    result.sort((a, b) => (b.progress || 0) - (a.progress || 0))
  }
  
  return result
})

const emptyText = computed(() => {
  if (filter.value === 'learning') return '没有正在学习的课程'
  if (filter.value === 'finished') return '还没有完成的课程'
  return '暂无已购课程，快去选购吧~'
})

function formatTime(time: string) {
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

function getStudyButtonText(course: UserCourseInfo): string {
  if (course.isFinished) return '复习课程'
  if (course.progress && course.progress > 0) return '继续学习'
  return '开始学习'
}

function filterCourses() {
  // 筛选由computed自动处理
}

function sortCourses() {
  // 排序由computed自动处理
}

function goStudy(course: UserCourseInfo) {
  router.push(`/study/${course.courseId}`)
}

function handleCommand(command: string, course: UserCourseInfo) {
  if (command === 'detail') {
    router.push(`/course/${course.courseId}`)
  } else if (command === 'review') {
    router.push(`/course/${course.courseId}?tab=reviews`)
  }
}

async function loadCourses() {
  loading.value = true
  try {
    const res = await getUserCourses()
    courses.value = res.data || []
  } catch (e) {
    console.error('加载课程失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadCourses)
</script>

<style lang="scss" scoped>
.courses-page { 
  background: var(--bg-color); 
  min-height: calc(100vh - 90px); 
  padding: 20px 0; 
}

.container { 
  max-width: 950px; 
  margin: 0 auto; 
  padding: 16px; 
  background: #fff; 
  border-radius: 12px; 
}

.page-header {
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  
  h2 { font-size: 18px; font-weight: 600; }
  
  .stats {
    display: flex; 
    gap: 24px;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .num {
        font-size: 20px;
        font-weight: 700;
        color: var(--primary-color);
      }
      
      .label {
        font-size: 11px;
        color: var(--text-muted);
      }
    }
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  :deep(.el-radio-button__inner) { 
    font-size: 12px; 
    padding: 7px 16px; 
  }
}

.list {
  min-height: 200px;
}

.item { 
  display: flex; 
  align-items: center; 
  gap: 16px; 
  padding: 18px; 
  border: 1px solid #eee; 
  border-radius: 12px; 
  margin-bottom: 14px;
  transition: all 0.25s;
  
  &:hover {
    border-color: var(--primary-color);
    box-shadow: 0 6px 20px rgba(99, 102, 241, 0.12);
    transform: translateY(-2px);
  }
}

.course-cover {
  position: relative;
  width: 180px; 
  height: 100px; 
  border-radius: 10px; 
  overflow: hidden;
  cursor: pointer; 
  flex-shrink: 0;
  
  img { 
    width: 100%; 
    height: 100%; 
    object-fit: cover; 
    transition: transform 0.3s; 
  }
  
  .play-overlay {
    position: absolute; 
    top: 50%; 
    left: 50%; 
    transform: translate(-50%, -50%);
    width: 44px; 
    height: 44px; 
    background: rgba(99, 102, 241, 0.9); 
    border-radius: 50%;
    display: flex; 
    align-items: center; 
    justify-content: center;
    color: #fff; 
    font-size: 20px; 
    opacity: 0; 
    transition: all 0.3s;
  }
  
  .finished-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 24px;
    height: 24px;
    background: #10b981;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 14px;
  }
  
  &:hover {
    img { transform: scale(1.05); }
    .play-overlay { opacity: 1; }
  }
}

.item .info { 
  flex: 1;
  min-width: 0;
  
  h4 { 
    font-size: 15px; 
    font-weight: 600; 
    margin-bottom: 6px; 
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    
    &:hover { color: var(--primary-color); }
  }
  
  .teacher { 
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px; 
    color: var(--text-muted); 
    margin-bottom: 12px;
    
    .el-icon { font-size: 13px; }
  }
  
  .progress-section {
    margin-bottom: 10px;
    
    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;
    }
    
    .progress-label {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      color: var(--text-muted);
      
      .icon-success {
        color: #10b981;
      }
    }
    
    .progress-value {
      font-size: 13px;
      font-weight: 600;
      color: var(--primary-color);
      
      &.finished {
        color: #10b981;
      }
    }
    
    :deep(.el-progress-bar__outer) {
      border-radius: 4px;
    }
    
    :deep(.el-progress-bar__inner) {
      border-radius: 4px;
    }
  }
  
  .meta {
    font-size: 11px; 
    color: var(--text-muted);
    
    .last-study, .not-started {
      display: flex;
      align-items: center;
      gap: 4px;
      
      .el-icon { font-size: 13px; }
    }
    
    .not-started {
      color: #bbb;
    }
  }
}

.item .actions {
  display: flex; 
  flex-direction: column; 
  gap: 8px;
  
  .el-button {
    width: 120px;
    
    &.el-button--primary {
      .el-icon { margin-right: 4px; }
    }
  }
}
</style>
