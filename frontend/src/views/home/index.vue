<template>
  <div class="home-page">
    <!-- Hero区域 -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-side left">
          <div class="float-card c1"><span>Vue3</span></div>
          <div class="float-card c2"><span>React</span></div>
          <div class="float-card c3"><span>TypeScript</span></div>
        </div>
        <div class="hero-center">
          <h1><span class="gradient-text">涛涛在线教育</span></h1>
          <p class="slogan">— 让学习更简单 —</p>
          <el-button type="primary" round size="small" @click="router.push('/course')">浏览课程</el-button>
        </div>
        <div class="hero-side right">
          <div class="float-card c1"><span>Spring Boot</span></div>
          <div class="float-card c2"><span>Python</span></div>
          <div class="float-card c3"><span>MySQL</span></div>
        </div>
      </div>
    </section>

    <!-- 继续学习 - 仅登录用户显示 -->
    <section class="continue-learning" v-if="userStore.isLoggedIn && recentCourses.length">
      <div class="container">
        <div class="section-title">
          <h2>继续学习</h2>
          <router-link to="/user/courses" class="more">全部课程 →</router-link>
        </div>
        <div class="continue-list">
          <div v-for="course in recentCourses" :key="course.id" class="continue-item" @click="goStudy(course)">
            <div class="continue-cover">
              <img :src="course.courseCover || defaultCover" />
              <div class="play-btn">
                <el-icon><VideoPlay /></el-icon>
              </div>
            </div>
            <div class="continue-info">
              <h4>{{ course.courseTitle }}</h4>
              <p>{{ course.lessonTitle || '开始学习' }}</p>
              <div class="continue-progress">
                <el-progress :percentage="course.progress" :show-text="false" :stroke-width="4" />
                <span>{{ course.progress }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 分类 -->
    <section class="categories">
      <div class="container">
        <div class="section-title"><h2>热门分类</h2></div>
        <div class="category-list" v-loading="loadingCategories">
          <div 
            v-for="c in categories" 
            :key="c.id" 
            class="category-item" 
            @click="router.push({ path: '/course', query: { categoryId: c.id } })"
          >
            <div class="category-icon">{{ getCategoryIcon(c.name) }}</div>
            <span class="name">{{ c.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 课程 -->
    <section class="courses">
      <div class="container">
        <div class="section-title">
          <h2>热门课程</h2>
          <router-link to="/course" class="more">查看全部 →</router-link>
        </div>
        <div class="course-grid" v-loading="loadingCourses">
          <div v-for="course in courses" :key="course.id" class="course-card" @click="router.push(`/course/${course.id}`)">
            <div class="cover">
              <img :src="course.cover || defaultCover" />
              <span class="tag">{{ typeText(course.type) }}</span>
            </div>
            <div class="body">
              <h3>{{ course.title }}</h3>
              <div class="meta"><span>{{ course.teacherName }}</span><span>{{ course.studyCount }}人学习</span></div>
              <div class="foot">
                <span class="price" v-if="course.isFree">免费</span>
                <span class="price" v-else>¥{{ course.price }}</span>
                <span class="score">{{ course.score }}分</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新课程 -->
    <section class="new-courses">
      <div class="container">
        <div class="section-title">
          <h2>最新上架</h2>
          <router-link to="/course?orderBy=newest" class="more">更多 →</router-link>
        </div>
        <div class="new-course-list" v-loading="loadingNewCourses">
          <div v-for="course in newCourses" :key="course.id" class="new-course-item" @click="router.push(`/course/${course.id}`)">
            <img :src="course.cover || defaultCover" />
            <div class="new-course-info">
              <h4>{{ course.title }}</h4>
              <p>{{ course.teacherName }}</p>
              <span class="price" v-if="course.isFree">免费</span>
              <span class="price" v-else>¥{{ course.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 特色 -->
    <section class="features">
      <div class="container">
        <div class="section-title center"><h2>平台优势</h2></div>
        <div class="feature-list">
          <div class="feature-item"><div class="icon">🎯</div><h4>精准路径</h4><p>个性化学习方案</p></div>
          <div class="feature-item"><div class="icon">👨‍🏫</div><h4>专业讲师</h4><p>一线技术专家授课</p></div>
          <div class="feature-item"><div class="icon">👥</div><h4>社群学习</h4><p>和小伙伴一起进步</p></div>
          <div class="feature-item"><div class="icon">📱</div><h4>多端支持</h4><p>随时随地学习</p></div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { VideoPlay } from '@element-plus/icons-vue'
import { getCourseList, getCategoryTree } from '@/api/course'
import { getRecentStudyRecords, RecentStudyRecord } from '@/api/study'
import { useUserStore } from '@/stores/user'
import type { CourseListItem, CategoryTree } from '@/types/course'

const router = useRouter()
const userStore = useUserStore()
const loadingCourses = ref(false)
const loadingNewCourses = ref(false)
const loadingCategories = ref(false)
const courses = ref<CourseListItem[]>([])
const newCourses = ref<CourseListItem[]>([])
const categories = ref<CategoryTree[]>([])
const recentCourses = ref<RecentStudyRecord[]>([])
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400'

const categoryIcons: Record<string, string> = {
  '前端开发': '🌐',
  '后端开发': '⚙️',
  '移动开发': '📱',
  '人工智能': '🤖',
  '数据分析': '📊',
  'UI设计': '🎨',
  '数据库': '💾',
  '云计算': '☁️',
  '运维': '🔧',
  '测试': '🧪'
}

const typeText = (t: number) => ({ 1: '录播', 2: '直播', 3: '图文' }[t] || '录播')

function getCategoryIcon(name: string): string {
  return categoryIcons[name] || '📚'
}

function goStudy(course: RecentStudyRecord) {
  router.push(`/study/${course.courseId}?lesson=${course.lessonId}`)
}

async function loadCategories() {
  loadingCategories.value = true
  try {
    const res = await getCategoryTree()
    // 数字串行化后强转，取一级分类
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
.container { max-width: 1100px; margin: 0 auto; padding: 0 16px; }
.section-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; &.center { justify-content: center; } h2 { font-size: 16px; font-weight: 600; } .more { font-size: 11px; color: var(--primary-color); text-decoration: none; } }

.hero {
  min-height: 160px; position: relative; display: flex; align-items: center; overflow: hidden;
  .hero-bg { position: absolute; inset: 0; background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%); }
  .hero-content { position: relative; z-index: 1; max-width: 1100px; margin: 0 auto; padding: 24px 16px; display: flex; align-items: center; justify-content: space-between; width: 100%; }
  .hero-side { display: flex; flex-direction: column; gap: 10px; width: 120px;
    &.left { align-items: flex-end; }
    &.right { align-items: flex-start; }
    .float-card { padding: 7px 14px; background: rgba(255,255,255,0.1); border-radius: 6px; font-size: 11px; color: rgba(255,255,255,0.9); backdrop-filter: blur(4px); transition: all 0.3s; cursor: default;
      &.c1 { animation: floatLeft 3s ease-in-out infinite; }
      &.c2 { animation: floatLeft 3s ease-in-out infinite 0.5s; }
      &.c3 { animation: floatLeft 3s ease-in-out infinite 1s; }
    }
    &.right .float-card {
      &.c1 { animation: floatRight 3s ease-in-out infinite; }
      &.c2 { animation: floatRight 3s ease-in-out infinite 0.5s; }
      &.c3 { animation: floatRight 3s ease-in-out infinite 1s; }
    }
  }
  .hero-center { text-align: center; flex: 1; max-width: 400px;
    h1 { font-size: 26px; font-weight: 700; color: #fff; margin-bottom: 6px; .gradient-text { background: linear-gradient(135deg, #c4b5fd, #f0abfc, #a5b4fc); background-size: 200% 200%; -webkit-background-clip: text; -webkit-text-fill-color: transparent; animation: gradientShift 4s ease infinite; } }
    .slogan { font-size: 13px; color: rgba(255,255,255,0.6); margin-bottom: 14px; letter-spacing: 2px; font-style: italic; }
  }
}

@keyframes floatLeft { 0%, 100% { transform: translateX(0); } 50% { transform: translateX(-6px); } }
@keyframes floatRight { 0%, 100% { transform: translateX(0); } 50% { transform: translateX(6px); } }
@keyframes gradientShift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }

.continue-learning {
  background: linear-gradient(135deg, #f8fafc, #f0f4ff);
  padding: 32px 0;
  
  .continue-list {
    display: flex;
    gap: 16px;
  }
  
  .continue-item {
    flex: 1;
    display: flex;
    gap: 14px;
    padding: 16px;
    background: #fff;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid transparent;
    
    &:hover {
      border-color: var(--primary-color);
      box-shadow: 0 8px 24px rgba(99, 102, 241, 0.15);
      
      .play-btn { opacity: 1; transform: translate(-50%, -50%) scale(1); }
    }
    
    .continue-cover {
      position: relative;
      width: 120px;
      height: 75px;
      border-radius: 8px;
      overflow: hidden;
      flex-shrink: 0;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .play-btn {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%) scale(0.8);
        width: 36px;
        height: 36px;
        background: rgba(99, 102, 241, 0.9);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 16px;
        opacity: 0;
        transition: all 0.3s;
      }
    }
    
    .continue-info {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      h4 {
        font-size: 13px;
        font-weight: 600;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      p {
        font-size: 11px;
        color: var(--text-muted);
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .continue-progress {
        display: flex;
        align-items: center;
        gap: 8px;
        
        :deep(.el-progress) {
          flex: 1;
        }
        
        span {
          font-size: 11px;
          font-weight: 600;
          color: var(--primary-color);
          min-width: 32px;
        }
      }
    }
  }
}

.categories { background: #fff; padding: 32px 0;
  .category-list { display: flex; gap: 12px; flex-wrap: wrap; }
  .category-item { 
    padding: 12px 20px; background: #f5f7fa; border-radius: 10px; 
    display: flex; align-items: center; gap: 8px;
    cursor: pointer; transition: all 0.2s;
    
    .category-icon { font-size: 18px; }
    .name { font-size: 12px; }
    
    &:hover { background: var(--primary-color); color: #fff; transform: translateY(-2px); }
  }
}

.courses { background: #f8fafc; padding: 32px 0;
  .course-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
  .course-card { background: #fff; border-radius: 10px; overflow: hidden; cursor: pointer; transition: all 0.25s;
    &:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
    .cover { height: 110px; position: relative; overflow: hidden;
      img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
      .tag { position: absolute; top: 6px; left: 6px; padding: 2px 8px; background: rgba(0,0,0,0.6); color: #fff; font-size: 10px; border-radius: 10px; }
    }
    .body { padding: 10px; }
    h3 { font-size: 12px; font-weight: 600; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .meta { display: flex; justify-content: space-between; font-size: 10px; color: var(--text-muted); margin-bottom: 8px; }
    .foot { display: flex; justify-content: space-between; align-items: center; }
    .price { font-size: 14px; font-weight: 700; color: #ef4444; }
    .score { font-size: 10px; color: var(--text-muted); }
  }
}

.new-courses { background: #fff; padding: 32px 0;
  .new-course-list { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
  .new-course-item {
    display: flex; gap: 10px; padding: 12px; background: #f8fafc; border-radius: 10px;
    cursor: pointer; transition: all 0.2s;
    
    &:hover { background: #f0f4ff; transform: translateX(4px); }
    
    img { width: 80px; height: 50px; object-fit: cover; border-radius: 6px; }
    
    .new-course-info {
      flex: 1; min-width: 0;
      
      h4 { font-size: 12px; font-weight: 500; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      p { font-size: 10px; color: var(--text-muted); margin-bottom: 4px; }
      .price { font-size: 12px; font-weight: 700; color: #ef4444; }
    }
  }
}

.features { background: #f8fafc; padding: 32px 0;
  .feature-list { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
  .feature-item { padding: 20px 14px; background: #fff; border-radius: 10px; text-align: center; transition: all 0.2s;
    &:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
    .icon { font-size: 28px; margin-bottom: 8px; }
    h4 { font-size: 13px; font-weight: 600; margin-bottom: 4px; }
    p { font-size: 10px; color: var(--text-muted); }
  }
}

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
</style>
