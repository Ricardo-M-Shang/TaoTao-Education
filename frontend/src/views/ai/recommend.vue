<template>
  <div class="recommend-page">
    <div class="container">
      <!-- 头部区域 -->
      <div class="header-section">
        <div class="header-left">
          <div class="icon-box">
            <el-icon :size="24"><MagicStick /></el-icon>
          </div>
          <div class="header-text">
            <h1>AI 智能推荐</h1>
            <p>{{ result?.summary || '基于您的学习轨迹，定制专属学习方案' }}</p>
          </div>
        </div>
        <div class="header-right">
          <el-button 
            class="refresh-btn"
            :loading="refreshing"
            @click="handleRefresh"
            round
          >
            <el-icon><Refresh /></el-icon>
            换一批
          </el-button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-wrapper">
        <div v-for="i in 4" :key="i" class="skeleton-item">
          <el-skeleton animated>
            <template #template>
              <div class="skeleton-content">
                <el-skeleton-item variant="image" style="width: 120px; height: 80px; border-radius: 8px;" />
                <div style="flex: 1; display: flex; flex-direction: column; gap: 8px;">
                  <el-skeleton-item variant="h3" style="width: 60%" />
                  <el-skeleton-item variant="text" style="width: 90%" />
                  <el-skeleton-item variant="text" style="width: 40%" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 推荐列表 -->
      <div v-else-if="result?.courses?.length" class="recommend-list">
        <div 
          v-for="(course, index) in result.courses" 
          :key="course.id"
          class="recommend-card"
          @click="goToCourse(course.id)"
        >
          <!-- 序号 -->
          <div class="rank-number">{{ String(index + 1).padStart(2, '0') }}</div>

          <!-- 课程信息 -->
          <div class="card-main">
            <div class="cover-wrapper">
              <el-image :src="course.cover" fit="cover" loading="lazy">
                <template #error>
                  <div class="image-placeholder"><el-icon><Picture /></el-icon></div>
                </template>
              </el-image>
              <div class="badge">{{ course.categoryName }}</div>
            </div>

            <div class="info-content">
              <div class="top-info">
                <h3 class="title" :title="course.title">{{ course.title }}</h3>
                <div class="ai-reason">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>推荐理由：{{ course.reason }}</span>
                </div>
              </div>

              <div class="bottom-info">
                <div class="meta-group">
                  <span class="teacher"><el-icon><User /></el-icon>{{ course.teacherName }}</span>
                  <span class="dot">·</span>
                  <span class="learners">{{ formatNumber(course.studyCount) }}人学过</span>
                  <span class="dot">·</span>
                  <span class="score"><el-icon><StarFilled /></el-icon>{{ course.score }}</span>
                </div>
                
                <div class="price-action">
                  <span class="price" :class="{ free: course.isFree == 1 }">
                    {{ course.isFree == 1 ? '免费' : `¥${course.price}` }}
                  </span>
                  <el-button type="primary" link class="action-btn">
                    去学习 <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-else
        description="暂无推荐内容，去多学点课程吧"
        :image-size="160"
      >
        <el-button type="primary" @click="router.push('/course')">浏览课程</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  MagicStick, Refresh, User, StarFilled, 
  ChatDotRound, Picture, ArrowRight
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
    if (res.code === 200) {
      result.value = res.data
    }
  } catch (error) {
    console.error(error)
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
      ElMessage.success('推荐已更新')
    }
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

const goToCourse = (id: string | number) => {
  router.push(`/course/${id}`)
}

const formatNumber = (num: number) => {
  if (!num) return 0
  return num >= 10000 ? (num / 10000).toFixed(1) + '万' : num
}
</script>

<style scoped lang="scss">
.recommend-page {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
  padding: 32px 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Header */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
  
  .header-left {
    display: flex;
    gap: 16px;
    align-items: center;
    
    .icon-box {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: linear-gradient(135deg, #4f46e5, #7c3aed);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
    }
    
    .header-text {
      h1 {
        font-size: 24px;
        font-weight: 700;
        color: #1e293b;
        margin-bottom: 4px;
      }
      p {
        font-size: 14px;
        color: #64748b;
      }
    }
  }
  
  .refresh-btn {
    color: #4f46e5;
    background: #fff;
    border-color: #e2e8f0;
    
    &:hover {
      background: #eef2ff;
      border-color: #4f46e5;
    }
    
    .el-icon { margin-right: 4px; }
  }
}

/* List Style */
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  gap: 24px;
  border: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.08);
    border-color: #e2e8f0;
    
    .action-btn {
      opacity: 1;
      transform: translateX(0);
    }
  }
  
  .rank-number {
    font-size: 32px;
    font-weight: 700;
    color: #f1f5f9;
    line-height: 1;
    font-family: monospace;
    letter-spacing: -2px;
  }
  
  .card-main {
    flex: 1;
    display: flex;
    gap: 20px;
  }
  
  .cover-wrapper {
    width: 160px;
    height: 100px;
    border-radius: 8px;
    overflow: hidden;
    position: relative;
    flex-shrink: 0;
    
    .el-image {
      width: 100%;
      height: 100%;
    }
    
    .badge {
      position: absolute;
      top: 6px;
      right: 6px;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(4px);
      color: #fff;
      font-size: 10px;
      padding: 2px 8px;
      border-radius: 4px;
    }
    
    .image-placeholder {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 100%;
      background: #f1f5f9;
      color: #94a3b8;
    }
  }
  
  .info-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 8px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .ai-reason {
      background: #f8fafc;
      border-radius: 6px;
      padding: 8px 12px;
      display: flex;
      gap: 8px;
      font-size: 13px;
      color: #475569;
      line-height: 1.5;
      
      .el-icon { 
        color: #4f46e5; 
        margin-top: 2px;
        flex-shrink: 0;
      }
      
      span {
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
    
    .bottom-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 12px;
      
      .meta-group {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;
        color: #64748b;
        
        .dot { color: #cbd5e1; }
        
        .teacher, .score {
          display: flex;
          align-items: center;
          gap: 4px;
        }
        
        .score { color: #eab308; font-weight: 500; }
      }
      
      .price-action {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .price {
          font-size: 16px;
          font-weight: 700;
          color: #ef4444;
          
          &.free { color: #22c55e; }
        }
        
        .action-btn {
          opacity: 0;
          transform: translateX(-10px);
          transition: all 0.2s;
        }
      }
    }
  }
}

/* Skeleton */
.loading-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .skeleton-item {
    background: #fff;
    padding: 20px;
    border-radius: 16px;
    border: 1px solid #f1f5f9;
    
    .skeleton-content {
      display: flex;
      gap: 20px;
    }
  }
}

@media (max-width: 768px) {
  .recommend-card {
    padding: 16px;
    
    .rank-number { display: none; }
    
    .card-main {
      flex-direction: column;
      gap: 12px;
      
      .cover-wrapper {
        width: 100%;
        height: 160px;
      }
      
      .bottom-info {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
        
        .price-action {
          width: 100%;
          justify-content: space-between;
          
          .action-btn {
            opacity: 1;
            transform: none;
          }
        }
      }
    }
  }
}
</style>
