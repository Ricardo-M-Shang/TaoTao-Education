<template>
  <div class="login-page">
    <div class="bg-decor"><div class="circle c1"></div><div class="circle c2"></div></div>
    <div class="login-box">
      <!-- 左侧 -->
      <div class="brand">
        <div class="brand-inner">
          <div class="logo"><span>涛</span></div>
          <h1>涛涛在线教育</h1>
          <p>让学习更简单</p>
          <div class="features">
            <div class="item"><span class="dot"></span><span>优质课程资源</span></div>
            <div class="item"><span class="dot"></span><span>专业讲师授课</span></div>
            <div class="item"><span class="dot"></span><span>个性化学习路径</span></div>
          </div>
        </div>
      </div>
      <!-- 右侧表单 -->
      <div class="form-side">
        <div class="form-inner">
          <h2>欢迎登录</h2>
          <p>登录账号，开始学习之旅</p>
          <el-form ref="formRef" :model="form" :rules="rules" class="form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
            </el-form-item>
            <div class="options">
              <el-checkbox v-model="rememberMe" label="记住我" size="small" />
              <a href="#">忘记密码？</a>
            </div>
            <el-form-item>
              <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
            </el-form-item>
          </el-form>
          <div class="footer">还没有账号？<router-link to="/register">立即注册</router-link></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.loginAction(form)
      ElMessage.success('登录成功')
      router.push((route.query.redirect as string) || '/')
    } catch (e) { console.error(e) }
    finally { loading.value = false }
  })
}
</script>

<style lang="scss" scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea, #764ba2); position: relative; overflow: hidden; }
.bg-decor { position: absolute; inset: 0; pointer-events: none;
  .circle { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.08); animation: float 6s ease-in-out infinite;
    &.c1 { width: 200px; height: 200px; top: -60px; right: -40px; }
    &.c2 { width: 150px; height: 150px; bottom: -40px; left: -30px; animation-delay: 2s; }
  }
}
.login-box { display: flex; width: 720px; min-height: 400px; background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.2); position: relative; z-index: 1; }

.brand { flex: 1; background: linear-gradient(135deg, #1e1b4b, #312e81); padding: 32px; display: flex; align-items: center; position: relative;
  &::before { content: ''; position: absolute; top: -40%; right: -40%; width: 80%; height: 80%; background: radial-gradient(circle, rgba(139,92,246,0.3) 0%, transparent 70%); }
  .brand-inner { position: relative; z-index: 1; }
  .logo { width: 44px; height: 44px; background: linear-gradient(135deg, #6366f1, #8b5cf6); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 18px; margin-bottom: 14px; box-shadow: 0 6px 16px rgba(99,102,241,0.4); }
  h1 { font-size: 20px; color: #fff; margin-bottom: 6px; }
  p { font-size: 11px; color: rgba(255,255,255,0.7); margin-bottom: 24px; }
  .features { display: flex; flex-direction: column; gap: 10px; }
  .item { display: flex; align-items: center; gap: 8px; font-size: 11px; color: rgba(255,255,255,0.8); padding: 8px 12px; background: rgba(255,255,255,0.08); border-radius: 8px;
    .dot { width: 6px; height: 6px; background: #a5b4fc; border-radius: 50%; }
  }
}

.form-side { flex: 1; display: flex; align-items: center; justify-content: center; padding: 32px; }
.form-inner { width: 100%; max-width: 260px;
  h2 { font-size: 18px; margin-bottom: 4px; }
  > p { font-size: 11px; color: var(--text-muted); margin-bottom: 20px; }
  .form :deep(.el-form-item) { margin-bottom: 14px; }
  .form :deep(.el-input__wrapper) { padding: 6px 10px; border-radius: 8px; }
  .form :deep(.el-input__inner) { font-size: 12px; }
  .options { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; font-size: 11px; a { color: var(--primary-color); text-decoration: none; } }
  .submit-btn { width: 100%; height: 36px; font-size: 13px; border-radius: 8px; }
  .footer { text-align: center; margin-top: 16px; font-size: 11px; color: var(--text-muted); a { color: var(--primary-color); text-decoration: none; margin-left: 4px; } }
}

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-12px); } }
</style>
