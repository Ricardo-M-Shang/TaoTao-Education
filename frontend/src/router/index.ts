import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'course',
        name: 'CourseList',
        component: () => import('@/views/course/list.vue'),
        meta: { title: '课程列表' }
      },
      {
        path: 'course/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/detail.vue'),
        meta: { title: '课程详情' }
      },
      {
        path: 'study/:id',
        name: 'CourseStudy',
        component: () => import('@/views/course/study.vue'),
        meta: { title: '课程学习', requireAuth: true }
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '个人中心', requireAuth: true, role: 1 }
      },
      {
        path: 'user/orders',
        name: 'UserOrders',
        component: () => import('@/views/user/orders.vue'),
        meta: { title: '我的订单', requireAuth: true, role: 1 }
      },
      {
        path: 'user/courses',
        name: 'UserCourses',
        component: () => import('@/views/user/courses.vue'),
        meta: { title: '我的课程', requireAuth: true, role: 1 }
      },
      {
        path: 'user/favorites',
        name: 'UserFavorites',
        component: () => import('@/views/user/favorites.vue'),
        meta: { title: '我的收藏', requireAuth: true, role: 1 }
      },
      {
        path: 'user/statistics',
        name: 'UserStatistics',
        component: () => import('@/views/user/statistics.vue'),
        meta: { title: '学习统计', requireAuth: true, role: 1 }
      },
      {
        path: 'teacher',
        name: 'TeacherCenter',
        component: () => import('@/views/teacher/index.vue'),
        meta: { title: '讲师中心', requireAuth: true, role: 2 }
      },
      {
        path: 'teacher/courses',
        name: 'TeacherCourses',
        component: () => import('@/views/teacher/courses.vue'),
        meta: { title: '讲师课程', requireAuth: true, role: 2 }
      },
      {
        path: 'teacher/edit/:id?',
        name: 'TeacherCourseEdit',
        component: () => import('@/views/teacher/edit.vue'),
        meta: { title: '课程发布', requireAuth: true, role: 2 }
      },
      {
        path: 'teacher/students/:courseId',
        name: 'TeacherStudents',
        component: () => import('@/views/teacher/students.vue'),
        meta: { title: '课程学员', requireAuth: true, role: 2 }
      },
      {
        path: 'teacher/stats',
        name: 'TeacherStats',
        component: () => import('@/views/teacher/stats.vue'),
        meta: { title: '收益统计', requireAuth: true, role: 2 }
      },
      {
        path: 'teacher/profile',
        name: 'TeacherProfile',
        component: () => import('@/views/teacher/profile.vue'),
        meta: { title: '资料设置', requireAuth: true, role: 2 }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || 'TaoTao'} - TaoTao在线教育平台`

  // 需要登录权限的页面
  if (to.meta.requireAuth) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    // 角色校验（仅针对需要角色的路由）
    if (to.meta.role && userStore.userInfo?.role !== to.meta.role) {
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router

