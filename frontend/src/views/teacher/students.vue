<template>
  <div class="teacher-students-page" v-loading="loading">
    <div class="container">
      <div class="header">
        <div>
          <h2>课程学员</h2>
          <p class="sub">查看已支付的学员及学习进度</p>
        </div>
        <el-button @click="router.back()">返回</el-button>
      </div>

      <el-table :data="students" stripe size="small">
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="scope">
            {{ scope.row.username || scope.row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="courseTitle" label="课程" min-width="160" />
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="scope">
            <el-progress :percentage="scope.row.progress || 0" :stroke-width="6" />
          </template>
        </el-table-column>
        <el-table-column prop="isFinished" label="完成" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isFinished ? 'success' : 'info'" size="small">
              {{ scope.row.isFinished ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastStudyTime" label="最后学习" min-width="150">
          <template #default="scope">{{ formatTime(scope.row.lastStudyTime) }}</template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" min-width="150">
          <template #default="scope">{{ formatTime(scope.row.payTime) }}</template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !students.length" description="暂无学员" />
    </div>

    <!-- AI 助手悬浮按钮 -->
    <TeacherAIFloatButton />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCourseStudents, TeacherStudent } from '@/api/teacher'
import TeacherAIFloatButton from '@/components/TeacherAIFloatButton.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const students = ref<TeacherStudent[]>([])

function formatTime(time?: string) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

async function load() {
  const courseId = Number(route.params.courseId)
  if (!courseId) return
  loading.value = true
  try {
    const res = await getCourseStudents(courseId)
    students.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.teacher-students-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 1000px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
  h2 { font-size: 18px; margin-bottom: 4px; }
  .sub { font-size: 12px; color: var(--text-muted); }
}
</style>

