<template>
  <div class="login-container">
    <div class="login-card">
      <div class="header">
        <div class="logo">
          <span>涛</span>
        </div>
        <h1>欢迎回来</h1>
        <p class="subtitle">登录您的账号以继续学习</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" size="large">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名 / 手机号" 
            :prefix-icon="User" 
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            :prefix-icon="Lock" 
            show-password 
            @keyup.enter="handleLogin" 
          />
        </el-form-item>
        
        <div class="form-options">
          <el-checkbox v-model="rememberMe" label="记住我" />
          <a href="#" class="forgot-pwd">忘记密码？</a>
        </div>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin" round>
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer">
        还没有账号？
        <router-link to="/register" class="link">立即注册</router-link>
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
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  background-size: cover;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  animation: slideUp 0.5s ease-out;

  .header {
    text-align: center;
    margin-bottom: 32px;

    .logo {
      width: 48px;
      height: 48px;
      margin: 0 auto 16px;
      background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      span {
        color: white;
        font-weight: bold;
        font-size: 24px;
      }
    }

    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 8px;
    }

    .subtitle {
      color: #6b7280;
      font-size: 14px;
    }
  }

  .login-form {
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #e5e7eb inset;
      padding: 8px 12px;
      
      &.is-focus {
        box-shadow: 0 0 0 2px #4f46e5 inset;
      }
    }
    
    :deep(.el-form-item) {
      margin-bottom: 20px;
    }
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .forgot-pwd {
      font-size: 14px;
      color: #4f46e5;
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }

  .submit-btn {
    width: 100%;
    padding: 12px;
    font-weight: 500;
    font-size: 16px;
    background: linear-gradient(to right, #4f46e5, #7c3aed);
    border: none;
    
    &:hover {
      opacity: 0.9;
      transform: translateY(-1px);
    }
    
    &:active {
      transform: translateY(0);
    }
  }

  .footer {
    margin-top: 24px;
    text-align: center;
    font-size: 14px;
    color: #6b7280;

    .link {
      color: #4f46e5;
      text-decoration: none;
      font-weight: 500;
      margin-left: 4px;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
