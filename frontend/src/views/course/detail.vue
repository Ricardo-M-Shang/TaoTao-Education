<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="course">
      <!-- 头部 -->
      <div class="header">
        <div class="header-inner">
          <div class="cover"><img :src="course.cover || defaultCover" /></div>
          <div class="info">
            <h1>{{ course.title }}</h1>
            <p class="sub">{{ course.subtitle }}</p>
            <div class="meta">
              <span>{{ course.teacherName }}</span>
              <span>{{ course.lessonCount }}课时</span>
              <span>{{ course.studyCount }}人学习</span>
              <span>{{ course.score }}分</span>
            </div>
            <div class="action">
              <div class="price" v-if="course.isFree">免费</div>
              <div class="price" v-else>¥{{ course.price }}<del v-if="course.originalPrice > course.price">¥{{ course.originalPrice }}</del></div>
              <el-button type="primary" round :disabled="hasBought" @click="handleBuy">{{ hasBought ? '已购买' : (course.isFree ? '立即学习' : '立即购买') }}</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 内容 -->
      <div class="content">
        <div class="main">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="课程介绍" name="intro">
              <div class="intro">
                <h3>简介</h3>
                <p>{{ course.description }}</p>
                <div v-html="course.content"></div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="课程目录" name="chapters">
              <el-collapse v-model="activeChapters">
                <el-collapse-item v-for="ch in course.chapters" :key="ch.id" :name="ch.id">
                  <template #title><div class="ch-title"><span>{{ ch.title }}</span><span class="cnt">{{ ch.lessons?.length || 0 }}课时</span></div></template>
                  <div v-for="ls in ch.lessons" :key="ls.id" class="lesson">
                    <span class="name">{{ ls.title }}</span>
                    <span class="dur">{{ Math.floor(ls.duration/60) }}:{{ (ls.duration%60).toString().padStart(2,'0') }}</span>
                    <el-tag v-if="ls.isFree" size="small" type="success">试看</el-tag>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div class="side">
          <div class="teacher-card">
            <h4>讲师</h4>
            <el-avatar :size="48" />
            <div class="name">{{ course.teacherName }}</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseDetail } from '@/api/course'
import { createOrder, checkBuyCourse } from '@/api/order'
import { useUserStore } from '@/stores/user'
import type { CourseDetail } from '@/types/course'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const course = ref<CourseDetail | null>(null)
const activeTab = ref('intro')
const activeChapters = ref<number[]>([])
const hasBought = ref(false)
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=600'

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    const res = await getCourseDetail(id)
    course.value = res.data
    if (res.data.chapters?.length) activeChapters.value = [res.data.chapters[0].id]
    if (userStore.isLoggedIn) { const r = await checkBuyCourse(id); hasBought.value = r.data }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function handleBuy() {
  if (!userStore.isLoggedIn) { router.push({ name: 'Login', query: { redirect: route.fullPath } }); return }
  if (!course.value) return
  if (course.value.isFree) {
    await createOrder({ courseId: course.value.id, courseTitle: course.value.title, courseCover: course.value.cover, teacherName: course.value.teacherName, originalPrice: 0 })
    ElMessage.success('已加入学习')
    hasBought.value = true
    return
  }
  ElMessageBox.confirm(`确定购买「${course.value.title}」？`, '购买确认').then(async () => {
    await createOrder({ courseId: course.value!.id, courseTitle: course.value!.title, courseCover: course.value!.cover, teacherName: course.value!.teacherName, originalPrice: course.value!.price })
    ElMessage.success('订单已创建')
    router.push('/user/orders')
  }).catch(() => {})
}

onMounted(load)
</script>

<style lang="scss" scoped>
.detail-page { min-height: calc(100vh - 90px); }

.header { background: linear-gradient(135deg, #1e1b4b, #312e81); padding: 28px 0;
  .header-inner { max-width: 1100px; margin: 0 auto; padding: 0 16px; display: flex; gap: 24px; }
  .cover { width: 320px; height: 180px; border-radius: 10px; overflow: hidden; flex-shrink: 0; img { width: 100%; height: 100%; object-fit: cover; } }
  .info { color: #fff; flex: 1; }
  h1 { font-size: 20px; margin-bottom: 6px; }
  .sub { font-size: 12px; opacity: 0.8; margin-bottom: 12px; }
  .meta { display: flex; gap: 16px; font-size: 11px; opacity: 0.7; margin-bottom: 20px; }
  .action { display: flex; align-items: center; gap: 20px; }
  .price { font-size: 24px; font-weight: 700; color: #f87171; del { font-size: 12px; color: #999; margin-left: 6px; } }
}

.content { max-width: 1100px; margin: 0 auto; padding: 20px 16px; display: flex; gap: 20px;
  .main { flex: 1; background: #fff; border-radius: 10px; padding: 16px; }
  .side { width: 220px; flex-shrink: 0; }
}

.intro { h3 { font-size: 14px; margin-bottom: 10px; padding-bottom: 8px; border-bottom: 1px solid #eee; } p { font-size: 12px; color: #666; line-height: 1.6; } }

.ch-title { display: flex; justify-content: space-between; width: 100%; padding-right: 12px; font-size: 12px; .cnt { font-size: 10px; color: #999; } }
.lesson { display: flex; align-items: center; gap: 10px; padding: 8px 12px; border-bottom: 1px solid #f5f5f5; font-size: 11px; &:last-child { border: none; } .name { flex: 1; } .dur { color: #999; } }

.teacher-card { background: #fff; border-radius: 10px; padding: 16px; text-align: center; h4 { font-size: 12px; margin-bottom: 12px; } .name { margin-top: 8px; font-size: 13px; font-weight: 500; } }
</style>
