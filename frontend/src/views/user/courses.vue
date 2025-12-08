<template>
  <div class="courses-page">
    <div class="container">
      <h2>我的课程</h2>
      <div class="list" v-loading="loading">
        <el-empty v-if="!loading && !courses.length" description="暂无已购课程，快去选购吧~">
          <el-button type="primary" size="small" @click="$router.push('/course')">浏览课程</el-button>
        </el-empty>
        <div v-for="c in courses" :key="c.id" class="item">
          <img :src="c.courseCover || defaultCover" @click="$router.push(`/course/${c.courseId}`)" />
          <div class="info">
            <h4 @click="$router.push(`/course/${c.courseId}`)">{{ c.courseTitle }}</h4>
            <p>{{ c.teacherName }}</p>
            <div class="progress-row">
              <el-progress :percentage="c.progress || 0" :stroke-width="5" :status="c.isFinished ? 'success' : ''" />
              <span class="progress-text">{{ c.progress || 0 }}%</span>
            </div>
            <div class="meta">
              <span v-if="c.lastStudyTime">上次学习: {{ formatTime(c.lastStudyTime) }}</span>
              <span v-else>尚未开始学习</span>
            </div>
          </div>
          <el-button type="primary" size="small" round @click="$router.push(`/course/${c.courseId}`)">
            {{ c.progress > 0 ? '继续学习' : '开始学习' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUserCourses, UserCourseInfo } from '@/api/userCourse'

const loading = ref(false)
const courses = ref<UserCourseInfo[]>([])
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'

function formatTime(time: string) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
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
.courses-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 800px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 10px; }
h2 { font-size: 15px; margin-bottom: 14px; }

.item { 
  display: flex; align-items: center; gap: 14px; padding: 14px; 
  border: 1px solid #eee; border-radius: 8px; margin-bottom: 10px;
  transition: all 0.2s;
  
  &:hover {
    border-color: var(--primary-color);
    box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
  }
}

.item img { 
  width: 120px; height: 68px; object-fit: cover; border-radius: 6px; 
  cursor: pointer; transition: transform 0.2s;
  
  &:hover { transform: scale(1.02); }
}

.item .info { 
  flex: 1; 
  
  h4 { 
    font-size: 13px; margin-bottom: 4px; cursor: pointer;
    &:hover { color: var(--primary-color); }
  }
  
  p { font-size: 10px; color: var(--text-muted); margin-bottom: 8px; }
  
  .progress-row {
    display: flex; align-items: center; gap: 8px; margin-bottom: 4px;
    
    :deep(.el-progress) { flex: 1; max-width: 200px; }
    
    .progress-text { font-size: 10px; color: var(--text-muted); min-width: 30px; }
  }
  
  .meta {
    font-size: 10px; color: var(--text-muted);
  }
}
</style>
