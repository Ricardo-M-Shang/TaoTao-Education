<template>
  <div class="user-center">
    <div class="container">
      <UserSidebar active-menu="favorites" />
      
      <div class="main-content">
        <div class="content-panel">
          <div class="page-header">
            <h2>我的收藏</h2>
            <div class="header-actions" v-if="favorites.length">
              <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
              <el-button v-if="selectedIds.length" size="small" type="danger" plain @click="handleBatchRemove">
                批量取消 ({{ selectedIds.length }})
              </el-button>
            </div>
          </div>

          <div class="list" v-loading="loading">
            <el-empty v-if="!loading && !favorites.length" description="暂无收藏课程">
              <el-button type="primary" size="default" @click="$router.push('/course')">去浏览课程</el-button>
            </el-empty>

            <div v-for="f in favorites" :key="f.id" :class="['item', { selected: selectedIds.includes(f.id) }]">
              <el-checkbox :model-value="selectedIds.includes(f.id)" @change="(checked: any) => handleItemSelect(f.id, !!checked)" class="item-checkbox" />
              <div class="course-cover" @click="$router.push(`/course/${f.courseId}`)">
                <img :src="f.courseCover || defaultCover" />
                <span class="type-tag" v-if="f.isFree">免费</span>
              </div>
              <div class="info">
                <h4 @click="$router.push(`/course/${f.courseId}`)">{{ f.courseTitle }}</h4>
                <p class="teacher">讲师：{{ f.teacherName }}</p>
                <div class="meta">
                  <span class="price" v-if="f.isFree">免费</span>
                  <span class="price" v-else>¥{{ f.price }}</span>
                  <el-divider direction="vertical" />
                  <span class="study-count">{{ f.studyCount }}人学习</span>
                  <el-divider direction="vertical" />
                  <el-rate v-model="f.score" disabled :max="5" size="small" />
                  <span class="score-text">{{ f.score }}分</span>
                </div>
                <div class="collect-time">
                  <el-icon><Clock /></el-icon>
                  收藏于 {{ formatTime(f.createTime) }}
                </div>
              </div>
              <div class="actions">
                <el-button type="primary" size="small" round @click="goToCourse(f)">
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
                <el-button size="small" round @click="handleRemove(f)">
                  <el-icon><Delete /></el-icon>
                  取消收藏
                </el-button>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pager" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="pageNum"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next, total"
              @current-change="load"
              background
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, View, Delete } from '@element-plus/icons-vue'
import { getFavoriteList, removeFavorite, FavoriteInfo } from '@/api/favorite'
import UserSidebar from './components/UserSidebar.vue'

const router = useRouter()
const loading = ref(false)
const favorites = ref<FavoriteInfo[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'

// 批量选择
const selectedIds = ref<number[]>([])
const selectAll = computed({
  get: () => selectedIds.value.length === favorites.value.length && favorites.value.length > 0,
  set: () => {}
})

function handleSelectAll(val: boolean | string | number) {
  if (val) {
    selectedIds.value = favorites.value.map(f => f.id)
  } else {
    selectedIds.value = []
  }
}

function handleItemSelect(id: number, checked: boolean) {
  if (checked) {
    if (!selectedIds.value.includes(id)) {
      selectedIds.value.push(id)
    }
  } else {
    const index = selectedIds.value.indexOf(id)
    if (index > -1) {
      selectedIds.value.splice(index, 1)
    }
  }
}

function formatTime(time: string): string {
  if (!time) return ''
  return time.substring(0, 10)
}

function goToCourse(f: FavoriteInfo) {
  router.push(`/course/${f.courseId}`)
}

async function load() {
  loading.value = true
  try {
    const res = await getFavoriteList(pageNum.value, pageSize.value)
    favorites.value = res.data.records
    total.value = res.data.total
    selectedIds.value = []
  } catch (e) {
    console.error('加载收藏失败', e)
  } finally {
    loading.value = false
  }
}

async function handleRemove(favorite: FavoriteInfo) {
  ElMessageBox.confirm('确定取消收藏该课程？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await removeFavorite(favorite.courseId)
      ElMessage.success('已取消收藏')
      load()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

async function handleBatchRemove() {
  if (selectedIds.value.length === 0) return
  
  ElMessageBox.confirm(`确定取消收藏选中的 ${selectedIds.value.length} 门课程？`, '批量取消', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 批量取消收藏
      for (const id of selectedIds.value) {
        const favorite = favorites.value.find(f => f.id === id)
        if (favorite) {
          await removeFavorite(favorite.courseId)
        }
      }
      ElMessage.success('批量取消成功')
      load()
    } catch (e) {
      console.error(e)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

onMounted(load)
</script>

<style lang="scss" scoped>
.user-center {
  background: #f8fafc;
  min-height: calc(100vh - 60px);
  padding: 24px 0;
  color: #334155;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  gap: 24px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-panel {
  background: white;
  border-radius: 16px;
  padding: 32px;
  min-height: 500px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
  
  h2 { font-size: 20px; font-weight: 600; color: #0f172a; }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

.list {
  min-height: 200px;
}

.item { 
  display: flex; 
  align-items: center; 
  gap: 20px; 
  padding: 20px; 
  border: 1px solid #f1f5f9; 
  border-radius: 12px; 
  margin-bottom: 16px;
  transition: all 0.25s;
  background: #fff;
  
  &:hover {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
    transform: translateY(-2px);
  }
  
  &.selected {
    background: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary-light-5);
  }
  
  .item-checkbox {
    flex-shrink: 0;
  }
}

.course-cover {
  position: relative;
  width: 160px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  
  img { 
    width: 100%; 
    height: 100%; 
    object-fit: cover;
    transition: transform 0.3s;
  }
  
  &:hover img { transform: scale(1.05); }
  
  .type-tag {
    position: absolute;
    top: 6px;
    left: 6px;
    padding: 2px 8px;
    background: #10b981;
    color: #fff;
    font-size: 10px;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  }
}

.item .info { 
  flex: 1;
  min-width: 0;
  
  h4 { 
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 8px; 
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #0f172a;
    
    &:hover { color: var(--el-color-primary); }
  }
  
  .teacher {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 12px;
  }
  
  .meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    
    .price { 
      font-size: 16px; 
      font-weight: 700; 
      color: #ef4444; 
    }
    
    .study-count { 
      font-size: 12px; 
      color: #94a3b8; 
    }
    
    .el-rate {
      height: 16px;
    }
    
    .score-text {
      font-size: 12px;
      color: #f59e0b;
      font-weight: 600;
    }
    
    :deep(.el-divider--vertical) {
      margin: 0 4px;
      height: 12px;
    }
  }
  
  .collect-time {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #cbd5e1;
    
    .el-icon { font-size: 14px; }
  }
}

.item .actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .el-button {
    width: 120px;
    
    .el-icon { margin-right: 4px; }
  }
}

.pager { 
  margin-top: 30px; 
  display: flex; 
  justify-content: center; 
}
</style>
