<template>
  <div class="course-list-page">
    <div class="container">
      <div class="main-layout">
        <!-- 左侧过滤器 -->
        <aside class="filter-sidebar">
          <div class="filter-group">
            <h4 class="filter-title">分类</h4>
            <div class="filter-options">
              <div 
                :class="['filter-item', { active: !query.categoryId }]" 
                @click="query.categoryId = undefined; load()"
              >
                全部
              </div>
              <div 
                v-for="c in categories" 
                :key="c.id" 
                :class="['filter-item', { active: query.categoryId === c.id }]" 
                @click="query.categoryId = c.id; load()"
              >
                {{ c.name }}
              </div>
            </div>
          </div>

          <div class="filter-group">
            <h4 class="filter-title">类型</h4>
            <div class="filter-options">
              <div :class="['filter-item', { active: !query.type }]" @click="query.type = undefined; load()">全部</div>
              <div :class="['filter-item', { active: query.type === 1 }]" @click="query.type = 1; load()">录播</div>
              <div :class="['filter-item', { active: query.type === 2 }]" @click="query.type = 2; load()">直播</div>
              <div :class="['filter-item', { active: query.type === 3 }]" @click="query.type = 3; load()">图文</div>
            </div>
          </div>

          <div class="filter-group">
            <h4 class="filter-title">价格</h4>
            <div class="filter-options">
              <div :class="['filter-item', { active: query.isFree === undefined }]" @click="query.isFree = undefined; load()">全部</div>
              <div :class="['filter-item', { active: query.isFree === 1 }]" @click="query.isFree = 1; load()">免费</div>
              <div :class="['filter-item', { active: query.isFree === 0 }]" @click="query.isFree = 0; load()">付费</div>
            </div>
          </div>
          
          <div class="filter-group">
             <h4 class="filter-title">价格区间</h4>
             <div class="price-range">
                <el-input v-model.number="priceMin" placeholder="¥" size="small" />
                <span class="divider">-</span>
                <el-input v-model.number="priceMax" placeholder="¥" size="small" />
             </div>
             <el-button type="primary" size="small" style="width: 100%; margin-top: 8px;" :disabled="!canApplyPrice" @click="applyPriceRange">确定</el-button>
          </div>
        </aside>

        <!-- 右侧列表 -->
        <main class="content-area">
          <div class="toolbar">
            <div class="left-tools">
              <div class="sort-tabs">
                <span :class="{ active: query.orderBy === 'popular' }" @click="setOrder('popular')">综合排序</span>
                <span :class="{ active: query.orderBy === 'newest' }" @click="setOrder('newest')">最新发布</span>
                <span :class="{ active: query.orderBy === 'priceAsc' || query.orderBy === 'priceDesc' }" @click="togglePriceOrder">
                  价格 
                  <el-icon v-if="query.orderBy === 'priceAsc'"><CaretTop /></el-icon>
                  <el-icon v-else-if="query.orderBy === 'priceDesc'"><CaretBottom /></el-icon>
                  <el-icon v-else><DCaret /></el-icon>
                </span>
              </div>
            </div>
            <div class="right-tools">
              <el-input 
                v-model="query.keyword" 
                placeholder="搜索课程..." 
                class="search-input" 
                @keyup.enter="load"
                clearable
                @clear="load"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
            </div>
          </div>

          <div class="course-list" v-loading="loading">
            <div v-for="c in courses" :key="c.id" class="course-item" @click="router.push(`/course/${c.id}`)">
              <div class="cover-wrapper">
                 <img :src="c.cover || defaultCover" />
                 <div class="badge">{{ typeText(c.type) }}</div>
              </div>
              <div class="info-wrapper">
                <div class="main-info">
                  <h3 class="title" :title="c.title">{{ c.title }}</h3>
                  <div class="sub-info">
                    <span class="teacher">{{ c.teacherName }}</span>
                    <span class="separator">·</span>
                    <span class="lessons">{{ c.lessonCount }}课时</span>
                  </div>
                </div>
                <div class="meta-info">
                  <div class="price-block">
                    <span class="current-price free" v-if="c.isFree">免费</span>
                    <span class="current-price" v-else><small>¥</small>{{ c.price }}</span>
                  </div>
                  <span class="learners">{{ c.studyCount }}人已学</span>
                </div>
              </div>
            </div>
            <el-empty v-if="!loading && !courses.length" description="暂无相关课程" />
          </div>

          <div class="pager-wrapper" v-if="total > 0">
            <el-pagination 
              v-model:current-page="query.pageNum" 
              :page-size="query.pageSize" 
              :total="total" 
              background
              layout="prev, pager, next" 
              @current-change="load" 
            />
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, CaretTop, CaretBottom, DCaret } from '@element-plus/icons-vue'
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
  type: undefined,
  isFree: undefined,
  minPrice: undefined,
  maxPrice: undefined,
  orderBy: 'popular',
  pageNum: 1,
  pageSize: 10 // 改为10条，列表模式更适合
})

const priceMin = ref<number | undefined>(undefined)
const priceMax = ref<number | undefined>(undefined)

const canApplyPrice = computed(() => {
  if (priceMin.value != null && priceMin.value < 0) return false
  if (priceMax.value != null && priceMax.value < 0) return false
  if (priceMin.value != null && priceMax.value != null && priceMin.value > priceMax.value) return false
  return priceMin.value != null || priceMax.value != null
})

async function loadCategories() {
  try {
    const res = await getCategoryTree()
    categories.value = (res.data || [])
      .map(c => ({ ...c, level: Number(c.level) }))
      .filter(c => c.level === 1)
  } catch (e) { console.error(e) }
}

async function load() {
  loading.value = true
  try {
    const res = await getCourseList(query)
    courses.value = (res.data.records || []).map((c) => ({
      ...c,
      id: String(c.id),
      isFree: Number(c.isFree),
      price: Number(c.price),
      originalPrice: Number(c.originalPrice)
    }))
    total.value = res.data.total
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function setOrder(order: string) {
  query.orderBy = order as any
  query.pageNum = 1
  load()
}

function togglePriceOrder() {
  if (query.orderBy === 'priceAsc') {
    setOrder('priceDesc')
  } else {
    setOrder('priceAsc')
  }
}

function applyPriceRange() {
  if (!canApplyPrice.value) return
  query.minPrice = priceMin.value != null ? Number(priceMin.value) : undefined
  query.maxPrice = priceMax.value != null ? Number(priceMax.value) : undefined
  query.pageNum = 1
  load()
}

function resetFilters() {
  query.keyword = ''
  query.categoryId = route.query.categoryId ? Number(route.query.categoryId) : undefined
  query.type = undefined
  query.isFree = undefined
  query.minPrice = undefined
  query.maxPrice = undefined
  priceMin.value = undefined
  priceMax.value = undefined
  query.orderBy = 'popular'
  query.pageNum = 1
  load()
}

onMounted(() => { loadCategories(); load() })
</script>

<style lang="scss" scoped>
.course-list-page { 
  background-color: #f8fafc; 
  min-height: calc(100vh - 64px);
  padding: 24px 0;
}

.container { 
  max-width: 1200px; 
  margin: 0 auto; 
  padding: 0 24px; 
}

.main-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* 侧边栏样式 */
.filter-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  position: sticky;
  top: 24px;
  
  .filter-group {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .filter-title {
      font-size: 14px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 12px;
    }
    
    .filter-options {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .filter-item {
        padding: 6px 12px;
        font-size: 13px;
        color: #64748b;
        cursor: pointer;
        border-radius: 6px;
        transition: all 0.2s;
        
        &:hover {
          color: #4f46e5;
          background: #f1f5f9;
        }
        
        &.active {
          color: #4f46e5;
          background: #eef2ff;
          font-weight: 500;
        }
      }
    }
    
    .price-range {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .divider { color: #94a3b8; }
    }
  }
}

/* 主内容区样式 */
.content-area {
  flex: 1;
  min-width: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: #fff;
  padding: 12px 20px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  
  .sort-tabs {
    display: flex;
    gap: 24px;
    
    span {
      font-size: 14px;
      color: #64748b;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 4px;
      transition: color 0.2s;
      
      &:hover {
        color: #1e293b;
      }
      
      &.active {
        color: #4f46e5;
        font-weight: 600;
      }
      
      .el-icon { font-size: 12px; }
    }
  }
  
  .search-input {
    width: 240px;
    
    :deep(.el-input__wrapper) {
      border-radius: 99px;
      background: #f8fafc;
      box-shadow: none;
      border: 1px solid #e2e8f0;
      transition: all 0.2s;
      
      &:hover, &.is-focus {
        background: #fff;
        border-color: #4f46e5;
        box-shadow: 0 0 0 1px #4f46e5 inset;
      }
    }
  }
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-item {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  gap: 20px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
    border-color: #4f46e5;
    
    .cover-wrapper img {
      transform: scale(1.05);
    }
  }
  
  .cover-wrapper {
    width: 200px;
    height: 120px;
    border-radius: 8px;
    overflow: hidden;
    position: relative;
    flex-shrink: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s;
    }
    
    .badge {
      position: absolute;
      top: 8px;
      right: 8px;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(4px);
      color: #fff;
      font-size: 10px;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }
  
  .info-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 4px 0;
    
    .main-info {
      .title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin-bottom: 8px;
        line-height: 1.4;
      }
      
      .sub-info {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;
        color: #64748b;
        
        .separator { color: #cbd5e1; }
      }
    }
    
    .meta-info {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      
      .price-block {
        .current-price {
          font-size: 18px;
          font-weight: 700;
          color: #ef4444;
          
          small { font-size: 12px; font-weight: normal; margin-right: 1px; }
          
          &.free { color: #22c55e; }
        }
      }
      
      .learners {
        font-size: 12px;
        color: #94a3b8;
      }
    }
  }
}

.pager-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }
  
  .filter-sidebar {
    width: 100%;
    position: static;
  }
  
  .course-item {
    flex-direction: column;
    
    .cover-wrapper {
      width: 100%;
      height: 180px;
    }
  }
}
</style>
