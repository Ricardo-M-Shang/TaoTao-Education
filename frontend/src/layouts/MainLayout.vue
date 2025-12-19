<template>
  <div class="layout">
    <!-- 顶栏 -->
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/')">
          <div class="logo-icon"><span>涛</span></div>
          <span class="logo-text">涛涛在线教育</span>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">首页</router-link>
          <router-link to="/course" class="nav-item" :class="{ active: route.path.startsWith('/course') }">课程</router-link>
          <router-link v-if="isStudent" to="/ai/recommend" class="nav-item ai-nav" :class="{ active: route.path.startsWith('/ai') }">
            <span class="ai-icon">✨</span>AI推荐
          </router-link>
          <router-link v-if="isStudent" to="/study/center" class="nav-item" :class="{ active: route.path.startsWith('/study') }">学习中心</router-link>
          <router-link v-if="userStore.isLoggedIn" to="/chat" class="nav-item" :class="{ active: route.path.startsWith('/chat') }">聊天室</router-link>
          <router-link v-if="isOrg" to="/org" class="nav-item" :class="{ active: route.path.startsWith('/org') }">机构</router-link>
          <router-link v-if="isOps" to="/ops" class="nav-item" :class="{ active: route.path.startsWith('/ops') }">运营</router-link>
        </nav>
        <div class="user">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-trigger">
                <el-avatar :size="26" :src="userStore.userInfo?.avatar || defaultAvatar" />
                <span>{{ userStore.userInfo?.nickname || '用户' }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown-menu">
                  <el-dropdown-item command="user">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" divided disabled>学习功能</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="study-center">学习中心</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="study-records">学习记录</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="study-report">学习报告</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" divided disabled>我的信息</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="courses">我的课程</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="favorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="coupons">我的优惠券</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="statistics">学习统计</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" divided disabled>智能服务</el-dropdown-item>
                  <el-dropdown-item v-if="isStudent" command="ai-recommend">✨ AI推荐</el-dropdown-item>
                  <el-dropdown-item divided disabled>互动功能</el-dropdown-item>
                  <el-dropdown-item command="chat">聊天室</el-dropdown-item>
                  <el-dropdown-item v-if="isTeacher" divided disabled>讲师功能</el-dropdown-item>
                  <el-dropdown-item v-if="isTeacher" command="t-courses">讲师课程</el-dropdown-item>
                  <el-dropdown-item v-if="isTeacher" command="t-stats">收益统计</el-dropdown-item>
                  <el-dropdown-item v-if="isTeacher" command="t-profile">资料设置</el-dropdown-item>
                <el-dropdown-item v-if="isOrg" divided disabled>机构功能</el-dropdown-item>
                <el-dropdown-item v-if="isOrg" command="o-courses">课程审核</el-dropdown-item>
                <el-dropdown-item v-if="isOrg" command="o-orders">机构订单</el-dropdown-item>
              <el-dropdown-item v-if="isOrg" command="o-stats">收益统计</el-dropdown-item>
              <el-dropdown-item v-if="isOps" divided disabled>运营功能</el-dropdown-item>
              <el-dropdown-item v-if="isOps" command="op-center">运营中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button link size="small" @click="router.push('/login')">登录</el-button>
            <el-button type="primary" size="small" round @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主体 -->
    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="footer-inner">
        <span>© 2024 涛涛在线教育</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const isTeacher = computed(() => userStore.userInfo?.role === 2)
const isStudent = computed(() => userStore.userInfo?.role === 1)
const isOrg = computed(() => userStore.userInfo?.role === 4)
const isOps = computed(() => userStore.userInfo?.role === 5)

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录？', '提示').then(() => { userStore.logoutAction(); router.push('/') }).catch(() => {})
  } else if (cmd === 'user') {
    if (isTeacher.value) router.push('/teacher')
    else if (isOrg.value) router.push('/org')
    else router.push('/user')
  }
  else if (cmd === 'study-center') router.push('/study/center')
  else if (cmd === 'study-records') router.push('/study/records')
  else if (cmd === 'study-report') router.push('/study/report')
  else if (cmd === 'orders') router.push('/user/orders')
  else if (cmd === 'courses') router.push('/user/courses')
  else if (cmd === 'favorites') router.push('/user/favorites')
  else if (cmd === 'coupons') router.push('/user/coupons')
  else if (cmd === 'statistics') router.push('/user/statistics')
  else if (cmd === 'chat') router.push('/chat')
  else if (cmd === 'ai-recommend') router.push('/ai/recommend')
  else if (cmd === 't-courses') router.push('/teacher/courses')
  else if (cmd === 't-stats') router.push('/teacher/stats')
  else if (cmd === 't-profile') router.push('/teacher/profile')
  else if (cmd === 'o-courses') router.push('/org/courses')
  else if (cmd === 'o-orders') router.push('/org/orders')
  else if (cmd === 'o-stats') router.push('/org/stats')
  else if (cmd === 'op-center') router.push('/ops')
}
</script>

<style>
.user-dropdown-menu {
  max-height: 400px;
  overflow-y: auto;
}
</style>

<style lang="scss" scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }

.header { height: 48px; background: rgba(255,255,255,0.95); backdrop-filter: blur(12px); border-bottom: 1px solid rgba(0,0,0,0.04); position: fixed; top: 0; left: 0; right: 0; z-index: 1000;
  .header-inner { max-width: 1100px; height: 100%; margin: 0 auto; padding: 0 16px; display: flex; align-items: center; }
  .logo { display: flex; align-items: center; gap: 8px; cursor: pointer;
    .logo-icon { width: 28px; height: 28px; background: linear-gradient(135deg, #6366f1, #8b5cf6); border-radius: 7px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 14px; }
    .logo-text { font-size: 14px; font-weight: 600; background: linear-gradient(135deg, #6366f1, #8b5cf6); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
  }
  .nav { flex: 1; display: flex; margin-left: 36px; gap: 6px;
    .nav-item { padding: 6px 14px; font-size: 12px; color: var(--text-secondary); text-decoration: none; border-radius: 6px; transition: all 0.2s;
      &:hover { color: var(--primary-color); background: rgba(99,102,241,0.06); }
      &.active { color: var(--primary-color); background: rgba(99,102,241,0.1); font-weight: 500; }
      &.ai-nav { display: flex; align-items: center; gap: 4px; background: linear-gradient(135deg, rgba(102,126,234,0.1), rgba(118,75,162,0.1)); border: 1px solid rgba(102,126,234,0.2);
        .ai-icon { font-size: 12px; }
        &:hover { background: linear-gradient(135deg, rgba(102,126,234,0.2), rgba(118,75,162,0.2)); border-color: rgba(102,126,234,0.3); }
        &.active { background: linear-gradient(135deg, rgba(102,126,234,0.25), rgba(118,75,162,0.25)); border-color: rgba(102,126,234,0.4); }
      }
    }
  }
  .user { display: flex; align-items: center; gap: 10px;
    .user-trigger { display: flex; align-items: center; gap: 6px; padding: 4px 10px 4px 4px; background: rgba(99,102,241,0.05); border-radius: 20px; cursor: pointer; font-size: 12px; color: var(--text-primary);
      &:hover { background: rgba(99,102,241,0.1); }
    }
  }
}

.main { flex: 1; margin-top: 48px; }

.footer { background: linear-gradient(135deg, #1e1b4b, #312e81); padding: 16px 0;
  .footer-inner { max-width: 1100px; margin: 0 auto; text-align: center; color: rgba(255,255,255,0.6); font-size: 11px; }
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
