<template>
  <div class="teacher-courses-page">
    <div class="container">
      <div class="header">
        <div>
          <h2>我的课程</h2>
          <p class="sub">管理草稿 / 已发布 / 已下架的课程</p>
        </div>
        <el-button type="primary" round @click="goCreate">
          <el-icon><Plus /></el-icon> 发布新课程
        </el-button>
      </div>

      <div class="filter">
        <el-input v-model="query.keyword" placeholder="搜索课程标题" size="small" class="w200" @keyup.enter="load">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="query.status" placeholder="状态" size="small" class="w140" clearable @change="load">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="2" />
          <el-option label="已下架" :value="3" />
        </el-select>
        <el-select v-model="query.orderBy" placeholder="排序" size="small" class="w140" @change="load">
          <el-option label="最近更新" value="newest" />
          <el-option label="最热" value="popular" />
          <el-option label="价格" value="price" />
        </el-select>
      </div>

      <div class="list" v-loading="loading">
        <el-empty v-if="!loading && !courses.length" description="暂无课程">
          <el-button type="primary" size="small" @click="goCreate">创建课程</el-button>
        </el-empty>

        <div v-for="c in courses" :key="c.id" class="card">
          <img :src="c.cover || defaultCover" class="cover" />
          <div class="info">
            <div class="title-row">
              <h3>{{ c.title }}</h3>
              <el-tag :type="statusType(c.status)" size="small">{{ statusText(c.status) }}</el-tag>
            </div>
            <p class="meta">
              {{ c.teacherName }} · {{ c.categoryName || '未分组' }} · {{ c.lessonCount || 0 }} 课时
            </p>
            <div class="stats">
              <span>学习 {{ c.studyCount || 0 }}</span>
              <span>价格 {{ c.isFree ? '免费' : '¥' + (c.price || 0) }}</span>
              <span>评分 {{ c.score || 0 }}</span>
              <span>创建时间 {{ formatDate(c.createTime) }}</span>
            </div>
            <div class="actions">
              <el-button size="small" @click="goEdit(c.id)">编辑</el-button>
              <el-button size="small" @click="goStudents(c.id)">学员</el-button>
              <el-button size="small" type="primary" v-if="c.status !== 2" @click="handlePublish(c)">发布</el-button>
              <el-button size="small" type="warning" v-if="c.status === 2" @click="handleOffline(c)">下架</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="pager" v-if="page.total">
        <el-pagination
          v-model:current-page="query.pageNum"
          :page-size="query.pageSize"
          :total="page.total"
          layout="prev, pager, next"
          @current-change="load"
        />
      </div>
    </div>

    <!-- AI 助手悬浮按钮 -->
    <TeacherAIFloatButton />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getTeacherCourseList, publishCourse, offlineCourse } from '@/api/teacher'
import type { CourseListItem, CourseListResult, CourseListParams } from '@/types/course'
import TeacherAIFloatButton from '@/components/TeacherAIFloatButton.vue'

const router = useRouter()
const loading = ref(false)
const courses = ref<CourseListItem[]>([])
const page = reactive({ total: 0 })
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400'

const query = reactive<CourseListParams>({
  keyword: '',
  status: undefined,
  orderBy: 'newest',
  pageNum: 1,
  pageSize: 8
})

const statusText = (s?: number) => ({ 0: '草稿', 2: '已发布', 3: '已下架' }[s || 0] || '草稿')
const statusType = (s?: number) => ({ 0: 'info', 2: 'success', 3: 'warning' }[s || 0] as any)

function formatDate(time?: string) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

function goCreate() {
  router.push('/teacher/edit')
}

function goEdit(id: number | string) {
  router.push(`/teacher/edit/${id}`)
}

function goStudents(id: number | string) {
  router.push(`/teacher/students/${id}`)
}

async function handlePublish(c: CourseListItem) {
  ElMessageBox.confirm(`确认发布「${c.title}」？`, '提示').then(async () => {
    await publishCourse(c.id)
    ElMessage.success('已发布')
    load()
  }).catch(() => {})
}

async function handleOffline(c: CourseListItem) {
  ElMessageBox.confirm(`确认下架「${c.title}」？`, '提示').then(async () => {
    await offlineCourse(c.id)
    ElMessage.success('已下架')
    load()
  }).catch(() => {})
}

async function load() {
  loading.value = true
  try {
    const res = await getTeacherCourseList(query)
    const data = res.data as CourseListResult
    courses.value = (data.records || []).map(c => ({
      ...c,
      id: String(c.id),
      status: Number(c.status),
      isFree: Number(c.isFree),
      price: Number(c.price),
      originalPrice: Number(c.originalPrice)
    }))
    page.total = data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.teacher-courses-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 1000px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h2 { font-size: 18px; margin-bottom: 4px; }
  .sub { font-size: 12px; color: var(--text-muted); }
}
.filter { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap;
  .w200 { width: 200px; }
  .w140 { width: 140px; }
}
.list { display: flex; flex-direction: column; gap: 12px; }
.card { display: flex; gap: 14px; border: 1px solid #eee; border-radius: 10px; padding: 12px; }
.cover { width: 160px; height: 90px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.info { flex: 1; min-width: 0; }
.title-row { display: flex; justify-content: space-between; align-items: center; gap: 8px;
  h3 { font-size: 15px; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}
.meta { font-size: 12px; color: var(--text-muted); margin: 6px 0; }
.stats { display: flex; flex-wrap: wrap; gap: 12px; font-size: 11px; color: var(--text-muted); }
.actions { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; }
.pager { margin-top: 16px; display: flex; justify-content: center; }
</style>

