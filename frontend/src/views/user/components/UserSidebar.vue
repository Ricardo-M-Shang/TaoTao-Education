<template>
  <div class="sidebar">
    <!-- 个人信息卡片 -->
    <div class="profile-card">
      <div class="avatar-wrapper">
        <el-avatar :size="80" :src="userStore.userInfo?.avatar || defaultAvatar" />
        <div class="avatar-badge" v-if="showEditBadge">
          <el-icon><Edit /></el-icon>
        </div>
      </div>
      <h2 class="nickname">{{ userStore.userInfo?.nickname || '用户' }}</h2>
      <p class="signature">{{ userStore.userInfo?.signature || '这个人很懒，什么都没写~' }}</p>
      
      <div class="quick-stats">
        <div class="qs-item">
          <span class="num">{{ stats.favorites }}</span>
          <span class="label">收藏</span>
        </div>
        <div class="divider"></div>
        <div class="qs-item">
          <span class="num">{{ stats.orders }}</span>
          <span class="label">订单</span>
        </div>
      </div>
    </div>

    <!-- 菜单 -->
    <div class="menu-wrapper">
      <el-menu
        :default-active="activeMenu"
        class="user-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="profile" @click="handleNav('profile', '/user')">
          <el-icon><User /></el-icon>
          <span>个人资料</span>
        </el-menu-item>
        <el-menu-item index="password" @click="handleNav('password', '/user')">
          <el-icon><Lock /></el-icon>
          <span>安全设置</span>
        </el-menu-item>
        <el-menu-item index="courses" @click="handleNav('courses', '/user/courses')">
          <el-icon><Reading /></el-icon>
          <span>我的课程</span>
        </el-menu-item>
        <el-menu-item index="orders" @click="handleNav('orders', '/user/orders')">
          <el-icon><Wallet /></el-icon>
          <span>我的订单</span>
        </el-menu-item>
        <el-menu-item index="statistics" @click="handleNav('statistics', '/user/statistics')">
          <el-icon><DataAnalysis /></el-icon>
          <span>学习统计</span>
        </el-menu-item>
        <el-menu-item index="favorites" @click="handleNav('favorites', '/user/favorites')">
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="coupons" @click="handleNav('coupons', '/user/coupons')">
          <el-icon><Ticket /></el-icon>
          <span>优惠券</span>
        </el-menu-item>
      </el-menu>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, Lock, Reading, DataAnalysis, Ticket, Edit, Wallet, Star
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getFavoriteList } from '@/api/favorite'
import { getOrderList } from '@/api/order'

const props = defineProps<{
  activeMenu: string
  showEditBadge?: boolean
}>()

const emit = defineEmits(['menu-select'])

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const stats = ref({
  favorites: 0,
  orders: 0
})

async function loadStats() {
  try {
    const [favRes, orderRes] = await Promise.all([
      getFavoriteList(1, 1),
      getOrderList({ pageNum: 1, pageSize: 1 })
    ])
    stats.value.favorites = favRes.data.total
    stats.value.orders = orderRes.data.total
  } catch (e) {
    console.error('Sidebar load stats failed', e)
  }
}

function handleMenuSelect(index: string) {
  emit('menu-select', index)
}

function handleNav(index: string, path: string) {
  // If we are on the main /user page (profile/password), we emit event to switch tab
  // If we are navigating to a different route, we push router
  if (path === '/user') {
    if (router.currentRoute.value.path === '/user') {
      // already on /user, just emit
      return 
    } else {
      // go to /user with query param to select tab? or just go
      // For simplicity, just go to /user and let it default to profile, or use state management
      // But index.vue defaults to 'profile'. 
      // We can pass a query param ?tab=password if needed, but for now just go.
      router.push(path)
    }
  } else {
    router.push(path)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style lang="scss" scoped>
.sidebar {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  
  .avatar-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 16px;
    
    .el-avatar {
      border: 4px solid #f1f5f9;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .avatar-badge {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 24px;
      height: 24px;
      background: var(--el-color-primary);
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      border: 2px solid white;
    }
  }
  
  .nickname {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 8px;
  }
  
  .signature {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 24px;
    line-height: 1.5;
  }
  
  .quick-stats {
    display: flex;
    justify-content: center;
    align-items: center;
    padding-top: 20px;
    border-top: 1px solid #f1f5f9;
    
    .qs-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 0 16px;
      
      .num {
        font-size: 16px;
        font-weight: 700;
        color: #0f172a;
      }
      .label {
        font-size: 12px;
        color: #94a3b8;
      }
    }
    
    .divider {
      width: 1px;
      height: 24px;
      background: #e2e8f0;
    }
  }
}

.menu-wrapper {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  
  .user-menu {
    border-right: none;
    
    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      margin: 4px 8px;
      border-radius: 8px;
      
      &.is-active {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        font-weight: 600;
      }
      
      &:hover:not(.is-active) {
        background: #f8fafc;
      }
      
      .el-icon {
        font-size: 18px;
        margin-right: 12px;
      }
    }
  }
}
</style>
