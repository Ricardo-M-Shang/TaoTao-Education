<template>
  <div class="teacher-center-page">
    <div class="container">
      <div class="header">
        <div>
          <h2>讲师中心</h2>
          <p class="sub">管理课程、学员与收益</p>
        </div>
      </div>

      <div class="grid">
        <div class="card" @click="go('/teacher/courses')">
          <div class="icon i1">📚</div>
          <div class="title">课程管理</div>
          <p>草稿 / 发布 / 下架</p>
        </div>
        <div class="card" @click="go('/teacher/edit')">
          <div class="icon i2">📝</div>
          <div class="title">发布课程</div>
          <p>创建新课程</p>
        </div>
        <div class="card" @click="go('/teacher/stats')">
          <div class="icon i3">💰</div>
          <div class="title">收益统计</div>
          <p>收入与订单数据</p>
        </div>
      </div>
    </div>
    
    <!-- AI 助手悬浮按钮 -->
    <TeacherAIFloatButton />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import TeacherAIFloatButton from '@/components/TeacherAIFloatButton.vue'

const router = useRouter()

function go(path: string) { router.push(path) }
</script>

<style scoped lang="scss">
.teacher-center-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 1000px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { margin-bottom: 16px;
  h2 { font-size: 18px; margin-bottom: 4px; }
  .sub { font-size: 12px; color: var(--text-muted); }
}

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; }
.card { padding: 16px; border: 1px solid #eee; border-radius: 10px; cursor: pointer; transition: all 0.2s;
  .icon { font-size: 28px; margin-bottom: 10px; }
  .title { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
  p { font-size: 12px; color: var(--text-muted); margin: 0; }
  &:hover { border-color: var(--primary-color); box-shadow: 0 6px 18px rgba(99,102,241,0.12); }
}
</style>

