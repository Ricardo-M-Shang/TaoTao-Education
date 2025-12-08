<template>
  <div class="register-page">
    <div class="bg-decor"><div class="circle c1"></div><div class="circle c2"></div></div>
    <div class="register-box">
      <!-- 表单 -->
      <div class="form-side">
        <div class="form-inner">
          <div class="back" @click="router.push('/login')">← 返回登录</div>
          <div class="logo"><span>涛</span></div>
          <h2>创建账号</h2>
          <p>加入涛涛在线教育</p>
          <el-form ref="formRef" :model="form" :rules="rules" class="form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名（4-20字符）" :prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码（6-20字符）" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input v-model="form.phone" placeholder="手机号（选填）" :prefix-icon="Phone" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="agreeTerms" size="small"><span class="terms">同意<a href="#">用户协议</a>和<a href="#">隐私政策</a></span></el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="submit-btn" :loading="loading" :disabled="!agreeTerms" @click="handleRegister">立即注册</el-button>
            </el-form-item>
          </el-form>
          <div class="footer">已有账号？<router-link to="/login">立即登录</router-link></div>
        </div>
      </div>
      <!-- 展示区 -->
      <div class="showcase">
        <div class="showcase-inner">
          <h3>为什么选择涛涛？</h3>
          <div class="points">
            <div class="point"><div class="num">01</div><div class="txt"><strong>优质课程</strong><span>精选行业优质内容</span></div></div>
            <div class="point"><div class="num">02</div><div class="txt"><strong>专业讲师</strong><span>一线技术专家授课</span></div></div>
            <div class="point"><div class="num">03</div><div class="txt"><strong>学习保障</strong><span>完善的学习服务</span></div></div>
          </div>
        </div>
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
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 4, max: 20, message: '4-20字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '6-20字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: (r, v, cb) => v !== form.password ? cb(new Error('密码不一致')) : cb(), trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
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
.register-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #ec4899, #8b5cf6); position: relative; overflow: hidden; }
.bg-decor { position: absolute; inset: 0; pointer-events: none;
  .circle { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.08); animation: float 7s ease-in-out infinite;
    &.c1 { width: 250px; height: 250px; top: -80px; left: -60px; }
    &.c2 { width: 180px; height: 180px; bottom: -50px; right: -40px; animation-delay: 3s; }
  }
}
.register-box { display: flex; width: 720px; min-height: 440px; background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.2); position: relative; z-index: 1; }

.form-side { flex: 1; display: flex; align-items: center; justify-content: center; padding: 28px; }
.form-inner { width: 100%; max-width: 260px;
  .back { font-size: 11px; color: var(--text-muted); cursor: pointer; margin-bottom: 14px; &:hover { color: var(--primary-color); } }
  .logo { width: 40px; height: 40px; background: linear-gradient(135deg, #ec4899, #8b5cf6); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 16px; margin-bottom: 12px; }
  h2 { font-size: 16px; margin-bottom: 4px; }
  > p { font-size: 11px; color: var(--text-muted); margin-bottom: 16px; }
  .form :deep(.el-form-item) { margin-bottom: 12px; }
  .form :deep(.el-input__wrapper) { padding: 5px 10px; border-radius: 8px; }
  .form :deep(.el-input__inner) { font-size: 12px; }
  .terms { font-size: 10px; color: var(--text-muted); a { color: var(--primary-color); text-decoration: none; } }
  .submit-btn { width: 100%; height: 34px; font-size: 12px; border-radius: 8px; &:disabled { opacity: 0.6; } }
  .footer { text-align: center; margin-top: 14px; font-size: 11px; color: var(--text-muted); a { color: var(--primary-color); text-decoration: none; } }
}

.showcase { flex: 1; background: linear-gradient(135deg, #1e1b4b, #312e81); padding: 28px; display: flex; align-items: center; position: relative;
  &::before { content: ''; position: absolute; inset: 0; background: radial-gradient(circle at 80% 20%, rgba(139,92,246,0.3) 0%, transparent 50%); }
  .showcase-inner { position: relative; z-index: 1; }
  h3 { font-size: 16px; color: #fff; margin-bottom: 20px; }
  .points { display: flex; flex-direction: column; gap: 12px; }
  .point { display: flex; gap: 12px; padding: 12px; background: rgba(255,255,255,0.08); border-radius: 10px;
    .num { font-size: 14px; font-weight: 700; color: #a5b4fc; }
    .txt { display: flex; flex-direction: column; gap: 2px; strong { font-size: 12px; color: #fff; } span { font-size: 10px; color: rgba(255,255,255,0.6); } }
  }
}

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
</style>
