<template>
  <div class="org-courses-page">
    <div class="container">
      <div class="header">
        <div>
          <h2>课程审核</h2>
          <p class="sub">待审核、已发布、已下架</p>
        </div>
      </div>

      <div class="filter">
        <el-input v-model="query.keyword" placeholder="搜索课程标题" size="small" class="w200" @keyup.enter="load">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="query.teacherId" placeholder="讲师" size="small" class="w160" clearable @change="onTeacherChange">
          <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" size="small" class="w140" clearable @change="load">
          <el-option label="待审核" :value="1" />
          <el-option label="已发布" :value="2" />
          <el-option label="已下架" :value="3" />
        </el-select>
      </div>

      <div class="list" v-loading="loading">
        <el-empty v-if="!loading && !courses.length" description="暂无课程" />
        <div v-for="c in courses" :key="c.id" class="card">
          <img :src="c.cover || defaultCover" class="cover" />
          <div class="info">
            <div class="title-row">
              <h3>{{ c.title }}</h3>
              <el-tag :type="statusType(c.status)" size="small">{{ statusText(c.status) }}</el-tag>
            </div>
            <p class="meta">
              {{ c.teacherName }} · {{ c.orgName || '未分配机构' }} · {{ c.categoryName || '未分组' }}
            </p>
            <div class="stats">
              <span>价格 {{ c.isFree ? '免费' : '¥' + (c.price || 0) }}</span>
              <span>课时 {{ c.lessonCount || 0 }}</span>
              <span>创建 {{ formatDate(c.createTime) }}</span>
            </div>
            <div class="actions">
              <el-button size="small" text type="primary" @click="showDetail(c)">详情</el-button>
              <el-button size="small" type="success" v-if="c.status === 1" @click="onApprove(c)">通过</el-button>
              <el-button size="small" type="danger" v-if="c.status === 1" @click="onReject(c)">拒绝</el-button>
              <el-button size="small" type="warning" v-if="c.status === 2" @click="onOffline(c)">下架</el-button>
            </div>
            <p v-if="c.auditRemark" class="remark">审核备注：{{ c.auditRemark }}</p>
          </div>
        </div>
      </div>

      <el-dialog v-model="detailVisible" title="课程详情" width="520px">
        <div v-if="detail">
          <div class="detail-header">
            <img :src="detail.cover || defaultCover" class="detail-cover" />
            <div class="detail-info">
              <h3>{{ detail.title }}</h3>
              <p class="sub">{{ detail.subtitle }}</p>
              <div class="tags">
                <el-tag size="small">{{ statusText(detail.status) }}</el-tag>
                <el-tag size="small" v-if="detail.orgName">机构：{{ detail.orgName }}</el-tag>
                <el-tag size="small" v-if="detail.teacherName">讲师：{{ detail.teacherName }}</el-tag>
              </div>
              <p class="line">分类：{{ detail.categoryName || '未分组' }}</p>
              <p class="line">价格：{{ detail.isFree ? '免费' : '¥' + (detail.price || 0) }}</p>
              <p class="line">课时：{{ detail.lessonCount || 0 }}</p>
              <p class="line" v-if="detail.auditRemark">审核备注：{{ detail.auditRemark }}</p>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="detailVisible = false">关闭</el-button>
        </template>
      </el-dialog>

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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getOrgCourseList, approveCourse, rejectCourse, offlineCourseOrg, getOrgTeachers } from '@/api/org'
import type { CourseListItem, CourseListParams, CourseListResult } from '@/types/course'
type TeacherOption = { id: number | string; name: string }

const loading = ref(false)
const courses = ref<CourseListItem[]>([])
const page = reactive({ total: 0 })
const detailVisible = ref(false)
const detail = ref<CourseListItem | null>(null)
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400'
const teachers = ref<TeacherOption[]>([])

const query = reactive<CourseListParams>({
  keyword: '',
  status: 1,
  orderBy: 'newest',
  pageNum: 1,
  pageSize: 8
})

const statusText = (s?: number) => ({ 0: '草稿', 1: '待审核', 2: '已发布', 3: '已下架' }[s || 0] || '草稿')
const statusType = (s?: number) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[s || 0] as any)

function formatDate(time?: string) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

async function loadTeachers() {
  try {
    const res = await getOrgTeachers()
    teachers.value = res.data || []
  } catch (e) { console.error(e) }
}

function onTeacherChange() {
  query.pageNum = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await getOrgCourseList(query)
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

function askRemark(title: string): Promise<string | undefined> {
  return new Promise(resolve => {
    ElMessageBox.prompt(title, '备注', { inputPlaceholder: '可选', confirmButtonText: '确定', cancelButtonText: '取消' })
      .then(({ value }) => resolve(value))
      .catch(() => resolve(undefined))
  })
}

async function onApprove(c: CourseListItem) {
  const remark = await askRemark(`通过「${c.title}」？`)
  await approveCourse(c.id, remark)
  ElMessage.success('已通过')
  load()
}

async function onReject(c: CourseListItem) {
  const remark = await askRemark(`拒绝「${c.title}」？`)
  await rejectCourse(c.id, remark)
  ElMessage.success('已拒绝')
  load()
}

async function onOffline(c: CourseListItem) {
  ElMessageBox.confirm(`确认下架「${c.title}」？`, '提示').then(async () => {
    await offlineCourseOrg(c.id)
    ElMessage.success('已下架')
    load()
  }).catch(() => {})
}

function showDetail(c: CourseListItem) {
  detail.value = c
  detailVisible.value = true
}

onMounted(() => { loadTeachers(); load() })
</script>

<style scoped lang="scss">
.org-courses-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 1000px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h2 { margin: 0; }
  .sub { color: var(--text-muted); font-size: 12px; }
}
.filter { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap;
  .w200 { width: 200px; }
  .w140 { width: 140px; }
}
.list { display: flex; flex-direction: column; gap: 12px; }
.card { display: flex; gap: 14px; border: 1px solid #eee; border-radius: 10px; padding: 12px; background: linear-gradient(135deg, #f8fafc, #ffffff); box-shadow: 0 8px 20px rgba(0,0,0,0.03); }
.cover { width: 140px; height: 80px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.info { flex: 1; min-width: 0; }
.title-row { display: flex; justify-content: space-between; align-items: center; gap: 8px;
  h3 { margin: 0; font-size: 15px; }
}
.meta { font-size: 12px; color: var(--text-muted); margin: 6px 0; }
.stats { display: flex; flex-wrap: wrap; gap: 12px; font-size: 11px; color: var(--text-muted); }
.actions { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; }
.remark { margin-top: 8px; font-size: 12px; color: #f59e0b; }
.detail-header { display: flex; gap: 12px; }
.detail-cover { width: 160px; height: 90px; object-fit: cover; border-radius: 8px; }
.detail-info { flex: 1; }
.detail-info h3 { margin: 0 0 6px 0; }
.detail-info .sub { margin: 0 0 6px 0; color: var(--text-muted); font-size: 12px; }
.detail-info .tags { display: flex; gap: 6px; margin-bottom: 6px; }
.detail-info .line { margin: 2px 0; font-size: 12px; color: var(--text-secondary); }
.pager { margin-top: 16px; display: flex; justify-content: center; }
</style>


