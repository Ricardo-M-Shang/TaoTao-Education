<template>
  <div class="course-list-page">
    <div class="container">
      <!-- 筛选 -->
      <div class="filter">
        <div class="row">
          <span class="label">分类</span>
          <div class="tags">
            <span :class="['tag', { active: !query.categoryId }]" @click="query.categoryId = undefined; load()">全部</span>
            <span v-for="c in categories" :key="c.id" :class="['tag', { active: query.categoryId === c.id }]" @click="query.categoryId = c.id; load()">{{ c.name }}</span>
          </div>
        </div>
        <div class="row">
          <span class="label">类型</span>
          <div class="tags">
            <span :class="['tag', { active: !query.type }]" @click="query.type = undefined; load()">全部</span>
            <span :class="['tag', { active: query.type === 1 }]" @click="query.type = 1; load()">录播</span>
            <span :class="['tag', { active: query.type === 2 }]" @click="query.type = 2; load()">直播</span>
            <span :class="['tag', { active: query.type === 3 }]" @click="query.type = 3; load()">图文</span>
          </div>
        </div>
        <div class="row">
          <span class="label">价格</span>
          <div class="tags">
            <span :class="['tag', { active: query.isFree === undefined }]" @click="query.isFree = undefined; load()">全部</span>
            <span :class="['tag', { active: query.isFree === 1 }]" @click="query.isFree = 1; load()">免费</span>
            <span :class="['tag', { active: query.isFree === 0 }]" @click="query.isFree = 0; load()">付费</span>
          </div>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="sorts">
          <span :class="{ active: query.orderBy === 'popular' }" @click="query.orderBy = 'popular'; load()">最热</span>
          <span :class="{ active: query.orderBy === 'newest' }" @click="query.orderBy = 'newest'; load()">最新</span>
          <span :class="{ active: query.orderBy === 'price' }" @click="query.orderBy = 'price'; load()">价格</span>
        </div>
        <el-input v-model="query.keyword" placeholder="搜索课程" class="search" @keyup.enter="load"><template #prefix><el-icon><Search /></el-icon></template></el-input>
      </div>

      <!-- 列表 -->
      <div class="grid" v-loading="loading">
        <div v-for="c in courses" :key="c.id" class="card" @click="router.push(`/course/${c.id}`)">
          <div class="cover"><img :src="c.cover || defaultCover" /><span class="tag">{{ typeText(c.type) }}</span></div>
          <div class="body">
            <h3>{{ c.title }}</h3>
            <div class="meta"><span>{{ c.teacherName }}</span><span>{{ c.lessonCount }}课时</span></div>
            <div class="foot">
              <span class="price" v-if="c.isFree">免费</span>
              <span class="price" v-else>¥{{ c.price }}</span>
              <span class="score">{{ c.score }}分</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && !courses.length" description="暂无课程" />
      </div>

      <!-- 分页 -->
      <div class="pager" v-if="total"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" layout="prev, pager, next" @current-change="load" /></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCourseList, getCategoryTree } from '@/api/course'
import type { CourseListItem, CategoryTree, CourseListParams } from '@/types/course'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const courses = ref<CourseListItem[]>([])
const categories = ref<CategoryTree[]>([])
const total = ref(0)
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400'
const typeText = (t: number) => ({ 1: '录播', 2: '直播', 3: '图文' }[t] || '录播')

const query = reactive<CourseListParams>({
  keyword: '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  type: undefined, isFree: undefined, orderBy: 'popular', pageNum: 1, pageSize: 12
})

async function loadCategories() {
  try { const res = await getCategoryTree(); categories.value = res.data.filter(c => c.level === 1) } catch (e) { console.error(e) }
}

async function load() {
  loading.value = true
  try { const res = await getCourseList(query); courses.value = res.data.records; total.value = res.data.total } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(() => { loadCategories(); load() })
</script>

<style lang="scss" scoped>
.course-list-page { background: #fff; min-height: calc(100vh - 90px); }
.container { max-width: 1100px; margin: 0 auto; padding: 20px 16px; }

.filter { background: #f8fafc; padding: 14px; border-radius: 10px; margin-bottom: 16px;
  .row { display: flex; align-items: center; margin-bottom: 8px; &:last-child { margin-bottom: 0; } }
  .label { width: 40px; font-size: 11px; color: var(--text-muted); flex-shrink: 0; }
  .tags { display: flex; flex-wrap: wrap; gap: 6px; }
  .tag { padding: 4px 12px; font-size: 11px; color: var(--text-secondary); background: #fff; border-radius: 14px; cursor: pointer; transition: all 0.2s;
    &:hover { color: var(--primary-color); }
    &.active { background: var(--primary-color); color: #fff; }
  }
}

.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  .sorts { display: flex; gap: 14px; span { font-size: 11px; color: var(--text-muted); cursor: pointer; padding-bottom: 2px; border-bottom: 2px solid transparent; &:hover, &.active { color: var(--primary-color); border-color: var(--primary-color); } } }
  .search { width: 180px; :deep(.el-input__wrapper) { border-radius: 16px; padding: 4px 10px; } :deep(.el-input__inner) { font-size: 11px; } }
}

.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; min-height: 200px; }

.card { background: #fff; border-radius: 10px; overflow: hidden; border: 1px solid #eee; cursor: pointer; transition: all 0.25s;
  &:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
  .cover { height: 100px; position: relative; overflow: hidden;
    img { width: 100%; height: 100%; object-fit: cover; }
    .tag { position: absolute; top: 6px; left: 6px; padding: 2px 8px; background: rgba(0,0,0,0.6); color: #fff; font-size: 9px; border-radius: 10px; }
  }
  .body { padding: 10px; }
  h3 { font-size: 12px; font-weight: 600; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .meta { display: flex; justify-content: space-between; font-size: 10px; color: var(--text-muted); margin-bottom: 8px; }
  .foot { display: flex; justify-content: space-between; align-items: center; }
  .price { font-size: 13px; font-weight: 700; color: #ef4444; }
  .score { font-size: 10px; color: var(--text-muted); }
}

.pager { margin-top: 24px; display: flex; justify-content: center; }
</style>
