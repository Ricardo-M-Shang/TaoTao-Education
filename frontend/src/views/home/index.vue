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

    <!-- 分类 -->
    <section class="categories">
      <div class="container">
        <div class="section-title"><h2>热门分类</h2></div>
        <div class="category-list">
          <div v-for="c in categories" :key="c.id" class="category-item" @click="router.push({ path: '/course', query: { categoryId: c.id } })">
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
        <div class="course-grid" v-loading="loading">
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

    <!-- 特色 -->
    <section class="features">
      <div class="container">
        <div class="section-title center"><h2>平台优势</h2></div>
        <div class="feature-list">
          <div class="feature-item"><div class="icon">◎</div><h4>精准路径</h4><p>个性化学习方案</p></div>
          <div class="feature-item"><div class="icon">◉</div><h4>专业讲师</h4><p>一线技术专家授课</p></div>
          <div class="feature-item"><div class="icon">◈</div><h4>社群学习</h4><p>和小伙伴一起进步</p></div>
          <div class="feature-item"><div class="icon">◇</div><h4>多端支持</h4><p>随时随地学习</p></div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCourseList } from '@/api/course'
import type { CourseListItem } from '@/types/course'

const router = useRouter()
const loading = ref(false)
const courses = ref<CourseListItem[]>([])
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400'

const categories = ref([
  { id: 1, name: '前端开发' },
  { id: 2, name: '后端开发' },
  { id: 3, name: '移动开发' },
  { id: 4, name: '人工智能' },
  { id: 5, name: '数据分析' },
  { id: 6, name: 'UI设计' }
])

const typeText = (t: number) => ({ 1: '录播', 2: '直播', 3: '图文' }[t] || '录播')

async function loadCourses() {
  loading.value = true
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 8, orderBy: 'popular' })
    courses.value = res.data.records
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(loadCourses)
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

.categories { background: #fff; padding: 32px 0;
  .category-list { display: flex; gap: 12px; flex-wrap: wrap; }
  .category-item { padding: 10px 20px; background: #f5f7fa; border-radius: 8px; font-size: 12px; cursor: pointer; transition: all 0.2s;
    &:hover { background: var(--primary-color); color: #fff; }
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

.features { background: #fff; padding: 32px 0;
  .feature-list { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
  .feature-item { padding: 20px 14px; background: #f8fafc; border-radius: 10px; text-align: center; transition: all 0.2s;
    &:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
    .icon { font-size: 20px; color: var(--primary-color); margin-bottom: 8px; }
    h4 { font-size: 13px; font-weight: 600; margin-bottom: 4px; }
    p { font-size: 10px; color: var(--text-muted); }
  }
}

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
</style>
