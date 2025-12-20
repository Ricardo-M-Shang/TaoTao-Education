<template>
  <div class="home-page">
    <!-- Hero区域 -->
    <section class="hero">
      <div class="hero-content container">
        <div class="hero-text">
          <h1>探索编程的<span class="highlight">无限可能</span></h1>
          <p class="slogan">专业课程，实战项目，助你成为卓越开发者</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="router.push('/course')" class="start-btn">
              开始学习 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
            <div class="stats">
              <div class="stat-item"><strong>100+</strong><span>精品课程</span></div>
              <div class="stat-item"><strong>10k+</strong><span>学员选择</span></div>
            </div>
          </div>
        </div>
        <div class="hero-visual">
          <div class="code-block">
            <div class="code-header">
              <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
            </div>
            <div class="code-body">
              <div class="line"><span class="k">const</span> <span class="v">learner</span> = <span class="k">new</span> <span class="c">Developer</span>();</div>
              <div class="line"><span class="v">learner</span>.<span class="m">study</span>(<span class="s">'TaoTao-Edu'</span>);</div>
              <div class="line"><span class="v">learner</span>.<span class="m">levelUp</span>();</div>
              <div class="line comment">// Start your journey today!</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 继续学习 - 仅登录用户显示 -->
    <section class="section continue-learning" v-if="userStore.isLoggedIn && recentCourses.length">
      <div class="container">
        <div class="section-header compact">
          <h3>继续学习</h3>
          <router-link to="/user/courses" class="link-more">全部课程 <el-icon><ArrowRight /></el-icon></router-link>
        </div>
        <div class="continue-grid">
          <div v-for="course in recentCourses" :key="course.id" class="continue-card" @click="goStudy(course)">
            <div class="card-cover">
              <img :src="course.courseCover || defaultCover" />
              <div class="progress-overlay" :style="{ width: course.progress + '%' }"></div>
            </div>
            <div class="card-info">
              <h4>{{ course.courseTitle }}</h4>
              <div class="meta">
                <span>{{ course.lessonTitle || '准备开始' }}</span>
                <span class="percent">{{ course.progress }}%</span>
              </div>
            </div>
            <div class="play-icon"><el-icon><VideoPlay /></el-icon></div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门领域 (紧凑版) -->
    <section class="section categories-compact">
      <div class="container">
        <div class="category-scroll-wrapper">
          <div 
            v-for="c in categories" 
            :key="c.id" 
            class="category-pill" 
            @click="router.push({ path: '/course', query: { categoryId: c.id } })"
          >
            <el-icon><component :is="getCategoryIconComponent(c.name)" /></el-icon>
            <span>{{ c.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 课程展示 (合并热门与最新) -->
    <section class="section courses-section">
      <div class="container">
        <div class="section-header">
          <div class="tabs">
            <h3 
              :class="{ active: activeTab === 'popular' }" 
              @click="activeTab = 'popular'"
            >热门课程</h3>
            <span class="divider">/</span>
            <h3 
              :class="{ active: activeTab === 'newest' }" 
              @click="activeTab = 'newest'"
            >最新上架</h3>
          </div>
          <router-link to="/course" class="link-more">查看全部 <el-icon><ArrowRight /></el-icon></router-link>
        </div>
        
        <div class="course-grid" v-if="activeTab === 'popular'" v-loading="loadingCourses">
          <div v-for="course in courses.slice(0, 4)" :key="course.id" class="course-card" @click="router.push(`/course/${course.id}`)">
            <div class="cover">
              <img :src="course.cover || defaultCover" />
              <div class="badge">{{ typeText(course.type) }}</div>
            </div>
            <div class="content">
              <h4 class="title" :title="course.title">{{ course.title }}</h4>
              <div class="footer">
                <span class="teacher">{{ course.teacherName }}</span>
                <div class="price-box">
                  <span class="price free" v-if="course.isFree">免费</span>
                  <span class="price" v-else><small>¥</small>{{ course.price }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="course-grid" v-else v-loading="loadingNewCourses">
          <div v-for="course in newCourses.slice(0, 4)" :key="course.id" class="course-card" @click="router.push(`/course/${course.id}`)">
            <div class="cover">
              <img :src="course.cover || defaultCover" />
              <div class="badge new">NEW</div>
            </div>
            <div class="content">
              <h4 class="title" :title="course.title">{{ course.title }}</h4>
              <div class="footer">
                <span class="teacher">{{ course.teacherName }}</span>
                <div class="price-box">
                  <span class="price free" v-if="course.isFree">免费</span>
                  <span class="price" v-else><small>¥</small>{{ course.price }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 特色 (更紧凑) -->
    <section class="section features-compact">
      <div class="container">
        <div class="feature-flex">
          <div class="feature-item">
            <el-icon><Aim /></el-icon>
            <span>精准路径</span>
          </div>
          <div class="separator"></div>
          <div class="feature-item">
            <el-icon><Medal /></el-icon>
            <span>专业讲师</span>
          </div>
          <div class="separator"></div>
          <div class="feature-item">
            <el-icon><ChatLineRound /></el-icon>
            <span>社群互动</span>
          </div>
          <div class="separator"></div>
          <div class="feature-item">
            <el-icon><Monitor /></el-icon>
            <span>多端支持</span>
          </div>
        </div>
      </div>
    </section>

    <!-- AI 助手悬浮按钮 -->
    <div class="ai-float-btn" @click="openAiChat" v-if="userStore.isLoggedIn">
      <div class="btn-content">
        <el-icon><ChatDotRound /></el-icon>
      </div>
    </div>

    <!-- AI 聊天窗口 -->
    <AIChatWindow v-model="chatVisible" :role="currentRole" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  VideoPlay, ChatDotRound, ArrowRight, User, 
  Monitor, Platform, Cpu, DataAnalysis, Brush, Connection, 
  Aim, Medal, ChatLineRound
} from '@element-plus/icons-vue'
import { getCourseList, getCategoryTree } from '@/api/course'
import { getRecentStudyRecords, RecentStudyRecord } from '@/api/study'
import { useUserStore } from '@/stores/user'
import AIChatWindow from '@/components/AIChatWindow.vue'
import type { CourseListItem, CategoryTree } from '@/types/course'

const router = useRouter()
const userStore = useUserStore()
const loadingCourses = ref(false)
const loadingNewCourses = ref(false)
const loadingCategories = ref(false)
const chatVisible = ref(false)
const courses = ref<CourseListItem[]>([])
const newCourses = ref<CourseListItem[]>([])
const categories = ref<CategoryTree[]>([])
const recentCourses = ref<RecentStudyRecord[]>([])
const activeTab = ref<'popular' | 'newest'>('popular')
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400'

const currentRole = computed(() => userStore.userInfo?.role || 1)

// 映射分类名称到 Element Plus 图标组件
const categoryIcons: Record<string, any> = {
  '前端开发': Monitor,
  '后端开发': Cpu,
  '移动开发': Platform,
  '人工智能': Cpu,
  '数据分析': DataAnalysis,
  'UI设计': Brush,
  '数据库': Connection,
  '云计算': Connection,
}

const typeText = (t: number) => ({ 1: '录播', 2: '直播', 3: '图文' }[t] || '录播')

function getCategoryIconComponent(name: string) {
  return categoryIcons[name] || Monitor
}

function goStudy(course: RecentStudyRecord) {
  router.push(`/study/${course.courseId}?lesson=${course.lessonId}`)
}

function openAiChat() {
  chatVisible.value = true
}

async function loadCategories() {
  loadingCategories.value = true
  try {
    const res = await getCategoryTree()
    categories.value = (res.data || [])
      .map(c => ({ ...c, level: Number(c.level) }))
      .filter(c => c.level === 1)
      .slice(0, 8)
  } catch (e) { console.error(e) }
  finally { loadingCategories.value = false }
}

async function loadCourses() {
  loadingCourses.value = true
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 8, orderBy: 'popular' })
    courses.value = (res.data.records || []).map((c) => ({
      ...c,
      id: String(c.id),
      isFree: Number(c.isFree),
      price: Number(c.price),
      originalPrice: Number(c.originalPrice)
    }))
  } catch (e) { console.error(e) }
  finally { loadingCourses.value = false }
}

async function loadNewCourses() {
  loadingNewCourses.value = true
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 4, orderBy: 'newest' })
    newCourses.value = (res.data.records || []).map((c) => ({
      ...c,
      id: String(c.id),
      isFree: Number(c.isFree),
      price: Number(c.price),
      originalPrice: Number(c.originalPrice)
    }))
  } catch (e) { console.error(e) }
  finally { loadingNewCourses.value = false }
}

async function loadRecentCourses() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getRecentStudyRecords(3)
    recentCourses.value = res.data || []
  } catch (e) { console.error(e) }
}

onMounted(() => {
  loadCategories()
  loadCourses()
  loadNewCourses()
  loadRecentCourses()
})
</script>

<style lang="scss" scoped>
.home-page {
  background-color: #f8fafc;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section {
  padding: 24px 0; // 减小垂直间距
  
  &-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    &.compact { margin-bottom: 12px; }

    .tabs {
      display: flex;
      align-items: baseline;
      gap: 12px;
      
      h3 {
        font-size: 18px;
        font-weight: 500;
        color: #94a3b8;
        cursor: pointer;
        transition: all 0.2s;
        
        &:hover { color: #64748b; }
        
        &.active {
          font-size: 20px;
          font-weight: 700;
          color: #1e293b;
          position: relative;
          
          &::after {
            content: '';
            position: absolute;
            left: 0;
            bottom: -6px;
            width: 20px;
            height: 3px;
            background: linear-gradient(to right, #4f46e5, #7c3aed);
            border-radius: 2px;
          }
        }
      }
      
      .divider { color: #cbd5e1; font-size: 16px; }
    }
    
    h3 {
      font-size: 18px;
      font-weight: 700;
      color: #1e293b;
    }
    
    .link-more {
      font-size: 12px;
      color: #64748b;
      text-decoration: none;
      display: flex;
      align-items: center;
      gap: 2px;
      transition: color 0.2s;
      
      &:hover { color: #4f46e5; }
    }
  }
}

/* Hero Section - 稍微减小高度 */
.hero {
  background: #fff;
  padding: 48px 0;
  border-bottom: 1px solid #f1f5f9;
  
  .hero-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 40px;
  }
  
  .hero-text {
    flex: 1;
    max-width: 540px;
    
    h1 {
      font-size: 36px;
      line-height: 1.2;
      color: #0f172a;
      font-weight: 800;
      margin-bottom: 12px;
      
      .highlight {
        background: linear-gradient(120deg, #4f46e5 0%, #9333ea 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
    }
    
    .slogan {
      font-size: 16px;
      color: #64748b;
      margin-bottom: 24px;
    }
    
    .hero-actions {
      display: flex;
      align-items: center;
      gap: 24px;
      
      .start-btn {
        padding: 10px 28px;
        font-weight: 600;
        border-radius: 99px;
      }
      
      .stats {
        display: flex;
        gap: 20px;
        
        .stat-item {
          display: flex;
          flex-direction: column;
          strong { font-size: 16px; color: #0f172a; }
          span { font-size: 11px; color: #64748b; }
        }
      }
    }
  }
  
  .hero-visual {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    
    .code-block {
      background: #1e293b;
      border-radius: 12px;
      padding: 16px;
      width: 100%;
      max-width: 420px;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
      transform: rotate(-1deg);
      
      .code-header {
        display: flex; gap: 6px; margin-bottom: 12px;
        .dot { width: 8px; height: 8px; border-radius: 50%; &.red { background: #ef4444; } &.yellow { background: #eab308; } &.green { background: #22c55e; } }
      }
      
      .code-body {
        font-family: 'Fira Code', monospace;
        font-size: 12px;
        line-height: 1.5;
        .line { margin-bottom: 2px; color: #e2e8f0; }
        .k { color: #c084fc; } .v { color: #38bdf8; } .c { color: #fbbf24; } .m { color: #a5b4fc; } .s { color: #4ade80; } .comment { color: #64748b; }
      }
    }
  }
}

/* Categories Compact */
.categories-compact {
  padding-top: 32px;
  padding-bottom: 8px;
  
  .category-scroll-wrapper {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
    
    .category-pill {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 16px;
      background: #fff;
      border: 1px solid #e2e8f0;
      border-radius: 99px;
      cursor: pointer;
      transition: all 0.2s;
      color: #475569;
      font-size: 13px;
      font-weight: 500;
      
      &:hover {
        border-color: #4f46e5;
        color: #4f46e5;
        background: #eef2ff;
        transform: translateY(-1px);
      }
      
      .el-icon { font-size: 14px; }
    }
  }
}

/* Continue Learning */
.continue-learning {
  .continue-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }
  
  .continue-card {
    background: #fff;
    border-radius: 8px;
    padding: 10px;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #f1f5f9;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
      border-color: #e2e8f0;
      .play-icon { background: #4f46e5; color: #fff; }
    }
    
    .card-cover {
      width: 70px;
      height: 44px;
      border-radius: 4px;
      overflow: hidden;
      position: relative;
      flex-shrink: 0;
      img { width: 100%; height: 100%; object-fit: cover; }
      .progress-overlay { position: absolute; bottom: 0; left: 0; height: 2px; background: #22c55e; }
    }
    
    .card-info {
      flex: 1; min-width: 0;
      h4 { font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      .meta { display: flex; justify-content: space-between; font-size: 11px; color: #64748b; .percent { font-weight: 600; color: #4f46e5; } }
    }
    
    .play-icon {
      width: 24px; height: 24px; border-radius: 50%; background: #f1f5f9; display: flex; align-items: center; justify-content: center; color: #94a3b8; transition: all 0.2s;
      font-size: 12px;
    }
  }
}

/* Courses Section */
.course-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.course-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
    .cover img { transform: scale(1.05); }
  }
  
  .cover {
    height: 120px; // 减小高度
    position: relative;
    overflow: hidden;
    
    img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
    
    .badge {
      position: absolute; top: 8px; right: 8px; background: rgba(0, 0, 0, 0.6); backdrop-filter: blur(4px); color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 4px;
      &.new { background: #ef4444; }
    }
  }
  
  .content {
    padding: 12px;
    
    .title {
      font-size: 14px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 8px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      height: 40px;
    }
    
    .footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .teacher { font-size: 11px; color: #94a3b8; }
      
      .price-box {
        .price {
          font-size: 14px; font-weight: 700; color: #ef4444;
          small { font-size: 11px; font-weight: normal; }
          &.free { color: #22c55e; }
        }
      }
    }
  }
}

/* Features Compact */
.features-compact {
  background: #fff;
  border-top: 1px solid #f1f5f9;
  padding: 24px 0;
  margin-top: 24px;
  
  .feature-flex {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 40px;
    color: #64748b;
    
    .feature-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      font-weight: 500;
      
      .el-icon { font-size: 18px; color: #94a3b8; }
    }
    
    .separator {
      width: 1px;
      height: 16px;
      background: #e2e8f0;
    }
  }
}

/* AI Float Button */
.ai-float-btn {
  position: fixed; bottom: 30px; right: 30px; z-index: 99; cursor: pointer;
  .btn-content {
    width: 48px; height: 48px; background: linear-gradient(135deg, #4f46e5, #7c3aed); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 24px; box-shadow: 0 4px 12px rgba(79, 70, 229, 0.4); transition: all 0.3s;
    &:hover { transform: scale(1.1); box-shadow: 0 8px 16px rgba(79, 70, 229, 0.5); }
  }
}

/* Responsive */
@media (max-width: 768px) {
  .hero {
    padding: 32px 0;
    .hero-content { flex-direction: column; text-align: center; }
    .hero-text .hero-actions { justify-content: center; }
    .hero-visual { display: none; }
  }
  
  .course-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .feature-flex {
    flex-wrap: wrap;
    gap: 20px;
    justify-content: space-around;
    .separator { display: none; }
  }
}
</style>
