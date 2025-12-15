<template>
  <div class="study-records">
    <div class="container">
      <div class="header">
        <h2>学习记录</h2>
        <div class="filters">
          <el-select 
            v-model="selectedCourse" 
            placeholder="选择课程" 
            clearable 
            style="width: 200px"
            @change="onCourseChange"
          >
            <el-option label="全部课程" value="" />
            <el-option 
              v-for="course in courseOptions" 
              :key="course.courseId" 
              :label="course.courseTitle" 
              :value="course.courseId" 
            />
          </el-select>
          <el-button @click="loadRecords" :loading="loading">刷新</el-button>
        </div>
      </div>

      <div class="records-content" v-loading="loading">
        <div v-if="records.length === 0" class="empty-state">
          <el-empty description="暂无学习记录">
            <el-button type="primary" @click="$router.push('/course')">去学习</el-button>
          </el-empty>
        </div>
        <div v-else class="records-list">
          <div v-for="record in records" :key="record.id" class="record-item">
            <div class="record-main">
              <div class="record-info">
                <h4 @click="goToCourse(record.courseId)">{{ record.courseTitle }}</h4>
                <p>{{ record.lessonTitle }}</p>
                <div class="record-meta">
                  <span class="date">{{ formatDate(record.studyDate) }}</span>
                  <span class="duration">学习时长: {{ formatDuration(record.studyDuration) }}</span>
                  <span class="device">{{ getDeviceText(record.deviceType) }}</span>
                  <el-tag 
                    v-if="record.isCompleted" 
                    type="success" 
                    size="small"
                  >
                    已完成
                  </el-tag>
                </div>
              </div>
              <div class="record-progress">
                <el-progress 
                  type="circle" 
                  :percentage="Math.round(record.progressPercent)" 
                  :width="80"
                  :stroke-width="6"
                >
                  <template #default="{ percentage }">
                    <span class="progress-text">{{ percentage }}%</span>
                  </template>
                </el-progress>
              </div>
            </div>
            <div class="record-details">
              <div class="detail-item">
                <span class="label">视频总时长:</span>
                <span class="value">{{ formatDuration(record.videoDuration) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">播放位置:</span>
                <span class="value">{{ formatDuration(record.lastPosition) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">学习时间:</span>
                <span class="value">{{ formatDateTime(record.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next, sizes, total"
            :page-sizes="[10, 20, 50]"
            @size-change="onPageSizeChange"
            @current-change="onPageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStudyRecords, getUserCourseStats, type StudyRecord, type LearningStats } from '@/api/learning'

const router = useRouter()

const loading = ref(false)
const records = ref<StudyRecord[]>([])
const courseOptions = ref<LearningStats[]>([])
const selectedCourse = ref<number | string>('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

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
    return date.toLocaleDateString('zh-CN', {
      month: 'numeric',
      day: 'numeric'
    })
  }
}

function formatDuration(seconds: number): string {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  if (hours > 0) {
    return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  } else {
    return `${minutes}:${secs.toString().padStart(2, '0')}`
  }
}

function formatDateTime(dateTimeStr: string): string {
  return new Date(dateTimeStr).toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getDeviceText(deviceType: string): string {
  const deviceMap: Record<string, string> = {
    'web': '网页端',
    'mobile': '手机端',
    'app': '移动应用',
    'tablet': '平板端'
  }
  return deviceMap[deviceType] || deviceType || '未知设备'
}

function goToCourse(courseId: number) {
  router.push(`/study/${courseId}`)
}

function onCourseChange() {
  currentPage.value = 1
  loadRecords()
}

function onPageChange(page: number) {
  currentPage.value = page
  loadRecords()
}

function onPageSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadRecords()
}

async function loadRecords() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...(selectedCourse.value && { courseId: Number(selectedCourse.value) })
    }
    
    const res = await getStudyRecords(params)
    records.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载学习记录失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadCourseOptions() {
  try {
    const res = await getUserCourseStats()
    courseOptions.value = res.data || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
  }
}

onMounted(() => {
  loadCourseOptions()
  loadRecords()
})
</script>

<style lang="scss" scoped>
.study-records {
  background: var(--bg-color);
  min-height: calc(100vh - 90px);
  padding: 20px 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }

  .filters {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

.records-content {
  background: white;
  border-radius: 12px;
  padding: 20px;
  min-height: 400px;
}

.records-list {
  .record-item {
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    margin-bottom: 16px;
    overflow: hidden;
    transition: all 0.2s;

    &:hover {
      border-color: var(--primary-color);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .record-main {
      display: flex;
      padding: 20px;
      align-items: center;
      gap: 20px;

      .record-info {
        flex: 1;

        h4 {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 4px;
          cursor: pointer;
          color: #333;

          &:hover {
            color: var(--primary-color);
          }
        }

        p {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;
        }

        .record-meta {
          display: flex;
          gap: 16px;
          font-size: 13px;
          color: #999;

          .date {
            color: var(--primary-color);
            font-weight: 500;
          }
        }
      }

      .record-progress {
        .progress-text {
          font-size: 14px;
          font-weight: 600;
          color: #333;
        }
      }
    }

    .record-details {
      padding: 16px 20px;
      background: #f8fafc;
      border-top: 1px solid #f0f0f0;
      display: flex;
      gap: 24px;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;

        .label {
          color: #666;
        }

        .value {
          color: #333;
          font-weight: 500;
        }
      }
    }
  }
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;

    .filters {
      justify-content: center;
    }
  }

  .record-item {
    .record-main {
      flex-direction: column;
      text-align: center;
      gap: 16px;
    }

    .record-details {
      flex-direction: column;
      gap: 12px;
    }
  }
}
</style>
