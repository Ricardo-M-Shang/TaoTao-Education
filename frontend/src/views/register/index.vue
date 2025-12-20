<template>
  <div class="register-container">
    <div class="register-card">
      <div class="header">
        <div class="logo">
          <span>涛</span>
        </div>
        <h1>创建账号</h1>
        <p class="subtitle">加入涛涛在线教育，开启学习之旅</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="register-form" size="large">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名（4-20字符）" 
            :prefix-icon="User" 
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码（6-20字符）" 
            :prefix-icon="Lock" 
            show-password 
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="确认密码" 
            :prefix-icon="Lock" 
            show-password 
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="手机号（选填）" 
            :prefix-icon="Phone" 
            clearable
          />
        </el-form-item>
        
        <el-form-item class="terms-item">
          <el-checkbox v-model="agreeTerms">
            我已阅读并同意 <a href="#" class="terms-link">用户协议</a> 和 <a href="#" class="terms-link">隐私政策</a>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            class="submit-btn" 
            :loading="loading" 
            :disabled="!agreeTerms" 
            @click="handleRegister" 
            round
          >
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer">
        已有账号？
        <router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const agreeTerms = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '', phone: '' })

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }, 
    { min: 4, max: 20, message: '4-20字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }, 
    { min: 6, max: 20, message: '6-20字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' }, 
    { validator: (r, v, cb) => v !== form.password ? cb(new Error('密码不一致')) : cb(), trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await register({ username: form.username, password: form.password, phone: form.phone || undefined })
      ElMessage.success('注册成功')
      router.push('/login')
    } catch (e) { console.error(e) }
    finally { loading.value = false }
  })
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  background-image: 
    radial-gradient(at 0% 100%, hsla(339,49%,30%,1) 0, transparent 50%), 
    radial-gradient(at 50% 100%, hsla(225,39%,30%,1) 0, transparent 50%), 
    radial-gradient(at 100% 100%, hsla(253,16%,7%,1) 0, transparent 50%);
  background-size: cover;
}

.register-card {
  width: 100%;
  max-width: 440px;
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
      background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
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

  .register-form {
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #e5e7eb inset;
      padding: 8px 12px;
      
      &.is-focus {
        box-shadow: 0 0 0 2px #ec4899 inset;
      }
    }
    
    :deep(.el-form-item) {
      margin-bottom: 20px;
    }

    .terms-item {
      margin-bottom: 24px;
      
      :deep(.el-checkbox__label) {
        font-size: 12px;
        color: #6b7280;
      }
    }
  }

  .terms-link {
    color: #ec4899;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }

  .submit-btn {
    width: 100%;
    padding: 12px;
    font-weight: 500;
    font-size: 16px;
    background: linear-gradient(to right, #ec4899, #8b5cf6);
    border: none;
    
    &:hover {
      opacity: 0.9;
      transform: translateY(-1px);
    }
    
    &:active {
      transform: translateY(0);
    }
    
    &:disabled {
      background: #e5e7eb;
      color: #9ca3af;
      transform: none;
    }
  }

  .footer {
    margin-top: 24px;
    text-align: center;
    font-size: 14px;
    color: #6b7280;

    .link {
      color: #ec4899;
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
