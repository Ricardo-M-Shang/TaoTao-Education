<template>
  <div class="recommend-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <div class="ai-icon">
            <el-icon :size="32"><MagicStick /></el-icon>
          </div>
          <div class="title-text">
            <h1>AI 智能推荐</h1>
            <p class="subtitle">{{ result?.summary || '根据您的学习历史，为您智能推荐课程' }}</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button 
            type="primary" 
            :icon="Refresh" 
            :loading="refreshing"
            @click="handleRefresh"
          >
            刷新推荐
          </el-button>
        </div>
      </div>
      
      <!-- 推荐信息 -->
      <div v-if="result" class="recommend-meta">
        <el-tag v-if="result.fromCache" type="info" size="small">
          <el-icon><Clock /></el-icon> 缓存结果
        </el-tag>
        <el-tag v-else type="success" size="small">
          <el-icon><Star /></el-icon> 最新推荐
        </el-tag>
        <span class="generate-time">
          生成时间: {{ formatTime(result.generatedAt) }}
        </span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="skeleton-grid">
        <div v-for="i in 5" :key="i" class="skeleton-card">
          <el-skeleton :rows="4" animated />
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty 
      v-else-if="!result?.courses?.length" 
      description="暂无推荐课程"
      :image-size="200"
    >
      <el-button type="primary" @click="loadRecommendations">重新加载</el-button>
    </el-empty>

    <!-- 推荐课程列表 -->
    <div v-else class="course-grid">
      <div 
        v-for="(course, index) in result.courses" 
        :key="course.id"
        class="course-card"
        :style="{ animationDelay: `${index * 0.1}s` }"
        @click="goToCourse(course.id)"
      >
        <!-- 推荐排名 -->
        <div class="rank-badge" :class="`rank-${index + 1}`">
          {{ index + 1 }}
        </div>

        <!-- 课程封面 -->
        <div class="course-cover">
          <el-image 
            :src="course.cover" 
            :alt="course.title"
            fit="cover"
            class="course-image"
            loading="lazy"
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
            <template #placeholder>
              <div class="image-slot">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="cover-overlay">
            <el-icon :size="40"><VideoPlay /></el-icon>
          </div>
          <div v-if="course.isFree == 1" class="free-tag">免费</div>
        </div>

        <!-- 课程信息 -->
        <div class="course-info">
          <h3 class="course-title" :title="course.title">{{ course.title }}</h3>
          <p class="course-subtitle" :title="course.subtitle">{{ course.subtitle }}</p>

          <!-- AI 推荐理由 -->
          <div class="ai-reason">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ course.reason }}</span>
          </div>

          <!-- 课程元信息 -->
          <div class="course-meta">
            <span class="teacher">
              <el-icon><User /></el-icon>
              {{ course.teacherName }}
            </span>
            <span class="category">
              <el-icon><Collection /></el-icon>
              {{ course.categoryName }}
            </span>
          </div>

          <!-- 统计数据 -->
          <div class="course-stats">
            <span class="study-count">
              <el-icon><View /></el-icon>
              {{ formatNumber(course.studyCount) }}人学习
            </span>
            <span v-if="course.score" class="score">
              <el-icon><Star /></el-icon>
              {{ course.score }}分
            </span>
          </div>

          <!-- 价格和操作 -->
          <div class="course-footer">
            <div class="price">
              <template v-if="course.isFree == 1">
                <span class="free">免费</span>
              </template>
              <template v-else>
                <span class="current">¥{{ course.price }}</span>
              </template>
            </div>
            <div class="recommend-score">
              <el-rate 
                :model-value="(course.recommendScore || 0) * 5" 
                disabled 
                :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                size="small"
                score-template="{value}"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多提示 -->
    <div v-if="result?.courses?.length" class="load-more-tip">
      <el-button text type="primary" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
        刷新获取新的推荐
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  MagicStick, Refresh, Clock, Star, VideoPlay, 
  User, Collection, View, ChatDotRound, Picture, Loading
} from '@element-plus/icons-vue'
import { getRecommendations, refreshRecommendations, type RecommendResult } from '@/api/ai'

const router = useRouter()
const loading = ref(false)
const refreshing = ref(false)
const result = ref<RecommendResult | null>(null)

onMounted(() => {
  loadRecommendations()
})

const loadRecommendations = async () => {
  loading.value = true
  try {
    const res = await getRecommendations({ count: 5 })
    if (res.code == 200) {
      result.value = res.data
    } else {
      ElMessage.error(res.message || '加载推荐失败')
    }
  } catch (error) {
    console.error('加载推荐失败:', error)
    ElMessage.error('加载推荐失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleRefresh = async () => {
  refreshing.value = true
  try {
    const res = await refreshRecommendations({ count: 5 })
    if (res.code == 200) {
      result.value = res.data
      ElMessage.success('推荐已刷新')
    } else {
      ElMessage.error(res.message || '刷新推荐失败')
    }
  } catch (error) {
    console.error('刷新推荐失败:', error)
    ElMessage.error('刷新推荐失败，请稍后重试')
  } finally {
    refreshing.value = false
  }
}

const goToCourse = (id: string | number) => {
  router.push(`/course/${id}`)
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatNumber = (num: number) => {
  if (!num) return 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num
}
</script>

<style scoped lang="scss">
.recommend-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
}

.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .title-section {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .ai-icon {
    width: 56px;
    height: 56px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  }

  .title-text {
    h1 {
      font-size: 24px;
      font-weight: 700;
      color: #1a1a2e;
      margin: 0 0 4px 0;
      font-family: 'Noto Sans SC', sans-serif;
    }

    .subtitle {
      font-size: 14px;
      color: #666;
      margin: 0;
    }
  }

  .recommend-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #eee;

    .el-tag {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .generate-time {
      font-size: 13px;
      color: #999;
    }
  }
}

.loading-container {
  .skeleton-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .skeleton-card {
    background: rgba(255, 255, 255, 0.9);
    border-radius: 12px;
    padding: 20px;
  }
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.course-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  height: 100%;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);

    .cover-overlay {
      opacity: 1;
    }
  }

  .rank-badge {
    position: absolute;
    top: 12px;
    left: 12px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 14px;
    z-index: 10;
    color: white;

    &.rank-1 {
      background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
      box-shadow: 0 4px 12px rgba(255, 170, 0, 0.4);
    }

    &.rank-2 {
      background: linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%);
      box-shadow: 0 4px 12px rgba(168, 168, 168, 0.4);
    }

    &.rank-3 {
      background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
      box-shadow: 0 4px 12px rgba(184, 115, 51, 0.4);
    }

    &.rank-4,
    &.rank-5 {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  }

  .course-cover {
    position: relative;
    height: 180px;
    overflow: hidden;
    flex-shrink: 0;

    .course-image {
      width: 100%;
      height: 100%;
      display: block;
    }

    .image-slot {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 100%;
      background: #f5f7fa;
      color: #909399;
      font-size: 30px;
    }

    .cover-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .free-tag {
      position: absolute;
      top: 12px;
      right: 12px;
      background: linear-gradient(135deg, #00c853 0%, #00e676 100%);
      color: white;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      z-index: 5;
    }
  }

  .course-info {
    padding: 16px;
    flex: 1;
    display: flex;
    flex-direction: column;

    .course-title {
      font-size: 16px;
      font-weight: 600;
      color: #1a1a2e;
      margin: 0 0 6px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      min-height: 44px;
      line-height: 1.4;
    }

    .course-subtitle {
      font-size: 13px;
      color: #666;
      margin: 0 0 12px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      min-height: 20px;
    }

    .ai-reason {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      padding: 10px 12px;
      background: linear-gradient(135deg, #f5f7fa 0%, #eff2f7 100%);
      border-radius: 8px;
      margin-bottom: 12px;
      border-left: 3px solid #667eea;
      min-height: 60px;

      .el-icon {
        color: #667eea;
        flex-shrink: 0;
        margin-top: 2px;
      }

      span {
        font-size: 13px;
        color: #555;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }

    .course-meta {
      display: flex;
      gap: 16px;
      margin-bottom: 8px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #888;

        .el-icon {
          font-size: 14px;
        }
      }
    }

    .course-stats {
      display: flex;
      gap: 16px;
      margin-bottom: 12px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #888;
      }

      .score {
        color: #ff9800;
      }
    }

    .course-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      border-top: 1px solid #eee;
      margin-top: auto;

      .price {
        .free {
          color: #00c853;
          font-weight: 600;
          font-size: 16px;
        }

        .current {
          color: #ff5722;
          font-weight: 700;
          font-size: 18px;
        }
      }

      .recommend-score {
        :deep(.el-rate) {
          height: auto;
        }
      }
    }
  }
}

.load-more-tip {
  margin-top: 48px;
  text-align: center;
  padding-bottom: 32px;

  .el-button {
    color: white;
    font-size: 15px;
    padding: 12px 24px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.1);
    
    &:hover {
      color: #fff;
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .recommend-container {
    padding: 16px;
  }

  .page-header {
    padding: 16px;

    .header-content {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }

    .header-actions {
      width: 100%;

      .el-button {
        width: 100%;
      }
    }
  }

  .course-grid {
    grid-template-columns: 1fr;
  }
}
</style>

