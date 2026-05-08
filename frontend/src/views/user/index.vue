<template>
  <div class="user-center">
    <div class="container">
      <!-- 左侧导航栏 -->
      <UserSidebar :active-menu="activeMenu" @menu-select="handleMenuSelect" />

      <!-- 右侧内容区 -->
      <div class="main-content">
        <!-- 顶部统计概览 -->
        <div class="stats-row">
          <div class="stat-card blue">
            <div class="icon-box"><el-icon><Reading /></el-icon></div>
            <div class="info">
              <div class="value">{{ stats.totalCourses }}</div>
              <div class="label">我的课程</div>
            </div>
          </div>
          <div class="stat-card purple">
            <div class="icon-box"><el-icon><Timer /></el-icon></div>
            <div class="info">
              <div class="value">{{ formatStudyTime(stats.totalStudyTime) }}</div>
              <div class="label">学习时长</div>
            </div>
          </div>
          <div class="stat-card orange">
            <div class="icon-box"><el-icon><Star /></el-icon></div>
            <div class="info">
              <div class="value">{{ stats.favorites }}</div>
              <div class="label">我的收藏</div>
            </div>
          </div>
          <div class="stat-card green">
            <div class="icon-box"><el-icon><Wallet /></el-icon></div>
            <div class="info">
              <div class="value">{{ stats.orders }}</div>
              <div class="label">购买订单</div>
            </div>
          </div>
        </div>

        <!-- 内容面板 -->
        <div class="content-panel">
          <transition name="fade-slide" mode="out-in">
            <!-- 个人资料 -->
            <div v-if="activeMenu === 'profile'" key="profile" class="panel-inner">
              <div class="panel-header">
                <h3>个人资料</h3>
                <p class="subtitle">管理您的个人信息和隐私设置</p>
              </div>
              
              <el-form 
                :model="profileForm" 
                label-position="top" 
                class="compact-form"
              >
                <div class="form-grid">
                  <el-form-item label="头像" class="avatar-item">
                    <div class="avatar-uploader-wrapper">
                       <el-upload
                        class="avatar-uploader"
                        :show-file-list="false"
                        :before-upload="beforeAvatarUpload"
                        :http-request="handleAvatarUpload"
                      >
                        <el-avatar :size="72" :src="profileForm.avatar || defaultAvatar" />
                        <div class="hover-mask"><el-icon><Camera /></el-icon></div>
                      </el-upload>
                      <div class="upload-tip">
                        <p>支持 JPG, PNG 格式</p>
                        <p>文件小于 2MB</p>
                      </div>
                    </div>
                  </el-form-item>

                  <el-form-item label="昵称">
                    <el-input v-model="profileForm.nickname" maxlength="20" show-word-limit placeholder="您的称呼" />
                  </el-form-item>

                  <el-form-item label="性别">
                    <el-radio-group v-model="profileForm.gender">
                      <el-radio-button :value="0">保密</el-radio-button>
                      <el-radio-button :value="1">男</el-radio-button>
                      <el-radio-button :value="2">女</el-radio-button>
                    </el-radio-group>
                  </el-form-item>

                  <el-form-item label="邮箱">
                    <el-input v-model="profileForm.email" placeholder="example@email.com" />
                  </el-form-item>

                  <el-form-item label="所在地区" class="full-width">
                    <div class="region-group">
                      <el-select v-model="profileForm.province" placeholder="省份" @change="handleProvinceChange">
                        <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
                      </el-select>
                      <el-select v-model="profileForm.city" placeholder="城市">
                        <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
                      </el-select>
                    </div>
                  </el-form-item>

                  <el-form-item label="个性签名" class="full-width">
                    <el-input 
                      v-model="profileForm.signature" 
                      type="textarea" 
                      :rows="3" 
                      maxlength="100" 
                      show-word-limit
                      placeholder="介绍一下自己..." 
                    />
                  </el-form-item>
                </div>

                <div class="form-actions">
                  <el-button type="primary" :loading="saving" @click="saveProfile">保存更改</el-button>
                </div>
              </el-form>
            </div>

            <!-- 修改密码 -->
            <div v-else-if="activeMenu === 'password'" key="password" class="panel-inner">
              <div class="panel-header">
                <h3>安全设置</h3>
                <p class="subtitle">定期修改密码可以保护您的账号安全</p>
              </div>

              <el-form 
                ref="pwdFormRef" 
                :model="pwdForm" 
                :rules="pwdRules" 
                label-position="top"
                class="compact-form password-form"
              >
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位字符" />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
                </el-form-item>
                
                <div class="form-actions">
                  <el-button type="primary" @click="changePwd">更新密码</el-button>
                  <el-button @click="resetPwdForm">重置</el-button>
                </div>
              </el-form>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules, UploadRawFile } from 'element-plus'
import { 
  Camera, DataAnalysis, Reading, Timer, Star, Wallet
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, changePassword } from '@/api/user'
import { uploadAvatar } from '@/api/file'
import { getStudyStatistics } from '@/api/study'
import { getFavoriteList } from '@/api/favorite'
import { getOrderList } from '@/api/order'
import UserSidebar from './components/UserSidebar.vue'

const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const activeMenu = ref('profile')
const pwdFormRef = ref<FormInstance>()
const saving = ref(false)

// 统计数据
const stats = ref({
  totalCourses: 0,
  totalStudyTime: 0,
  favorites: 0,
  orders: 0
})

function formatStudyTime(minutes: number): string {
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  return `${hours}小时`
}

async function loadStats() {
  try {
    const [studyRes, favRes, orderRes] = await Promise.all([
      getStudyStatistics(),
      getFavoriteList(1, 1),
      getOrderList({ pageNum: 1, pageSize: 1 })
    ])
    stats.value.totalCourses = studyRes.data.totalCourses
    stats.value.totalStudyTime = studyRes.data.totalStudyTime
    stats.value.favorites = favRes.data.total
    stats.value.orders = orderRes.data.total
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

// 省市数据
const regionData: Record<string, string[]> = {
  '北京': ['北京市'],
  '上海': ['上海市'],
  '天津': ['天津市'],
  '重庆': ['重庆市'],
  '辽宁': ['沈阳', '大连', '鞍山', '抚顺', '本溪', '丹东', '锦州', '营口', '盘锦'],
  '广东': ['广州', '深圳', '东莞', '佛山', '珠海', '惠州', '中山'],
  '浙江': ['杭州', '宁波', '温州', '嘉兴', '绍兴', '金华'],
  '江苏': ['南京', '苏州', '无锡', '常州', '南通', '扬州'],
  '四川': ['成都', '绵阳', '德阳', '宜宾', '泸州'],
  '湖北': ['武汉', '宜昌', '襄阳', '荆州', '黄石'],
  '湖南': ['长沙', '株洲', '湘潭', '岳阳', '衡阳'],
  '山东': ['济南', '青岛', '烟台', '威海', '潍坊'],
  '河南': ['郑州', '洛阳', '开封', '新乡', '南阳'],
  '河北': ['石家庄', '唐山', '保定', '廊坊', '邯郸'],
  '陕西': ['西安', '咸阳', '宝鸡', '渭南', '延安'],
  '福建': ['福州', '厦门', '泉州', '漳州', '莆田'],
  '安徽': ['合肥', '芜湖', '蚌埠', '马鞍山', '淮南'],
  '江西': ['南昌', '九江', '赣州', '景德镇'],
  '黑龙江': ['哈尔滨', '齐齐哈尔', '大庆', '牡丹江'],
  '吉林': ['长春', '吉林市', '四平', '延边']
}

const provinces = Object.keys(regionData)
const cities = computed(() => profileForm.province ? regionData[profileForm.province] || [] : [])

const profileForm = reactive({
  nickname: '',
  email: '',
  avatar: '',
  gender: 0,
  province: '',
  city: '',
  signature: ''
})

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '6-20字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: (r, v, cb) => v !== pwdForm.newPassword ? cb(new Error('密码不一致')) : cb(), trigger: 'blur' }]
}

function handleMenuSelect(i: string) { activeMenu.value = i }

function handleProvinceChange() {
  profileForm.city = ''
}

function beforeAvatarUpload(file: UploadRawFile) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

async function handleAvatarUpload(options: any) {
  try {
    const url = await uploadAvatar(options.file)
    profileForm.avatar = url
    ElMessage.success('头像上传成功')
  } catch (e) {
    console.error(e)
  }
}

async function saveProfile() {
  saving.value = true
  try {
    const email = profileForm.email?.trim()
    await updateUserInfo({
      nickname: profileForm.nickname,
      email: email || undefined,
      avatar: profileForm.avatar,
      gender: profileForm.gender,
      province: profileForm.province,
      city: profileForm.city,
      signature: profileForm.signature
    })
    ElMessage.success('保存成功')
    userStore.fetchUserInfo()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

async function changePwd() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async v => {
    if (!v) return
    try {
      await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
      ElMessage.success('密码已修改，请重新登录')
      userStore.logoutAction()
    } catch (e) {
      console.error(e)
    }
  })
}

function resetPwdForm() {
  if (pwdFormRef.value) {
    pwdFormRef.value.resetFields()
  }
}

onMounted(() => {
  if (userStore.userInfo) {
    profileForm.nickname = userStore.userInfo.nickname || ''
    profileForm.email = userStore.userInfo.email || ''
    profileForm.avatar = userStore.userInfo.avatar || ''
    profileForm.gender = userStore.userInfo.gender || 0
    profileForm.province = userStore.userInfo.province || ''
    profileForm.city = userStore.userInfo.city || ''
    profileForm.signature = userStore.userInfo.signature || ''
  }
  loadStats()
})
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

/* Sidebar Styles */
/* Moved to UserSidebar.vue */

/* Main Content Styles */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  
  .stat-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.02);
    transition: transform 0.2s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(0,0,0,0.05);
    }
    
    .icon-box {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: white;
    }
    
    .info {
      .value {
        font-size: 18px;
        font-weight: 700;
        color: #1e293b;
        line-height: 1.2;
      }
      .label {
        font-size: 12px;
        color: #64748b;
        margin-top: 4px;
      }
    }
    
    &.blue .icon-box { background: linear-gradient(135deg, #3b82f6, #2563eb); }
    &.purple .icon-box { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
    &.orange .icon-box { background: linear-gradient(135deg, #f59e0b, #d97706); }
    &.green .icon-box { background: linear-gradient(135deg, #10b981, #059669); }
  }
}

.content-panel {
  background: white;
  border-radius: 16px;
  padding: 32px;
  min-height: 500px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  
  .panel-header {
    margin-bottom: 30px;
    border-bottom: 1px solid #f1f5f9;
    padding-bottom: 20px;
    
    h3 {
      font-size: 20px;
      font-weight: 600;
      color: #0f172a;
      margin-bottom: 8px;
    }
    .subtitle {
      font-size: 13px;
      color: #94a3b8;
    }
  }
}

.compact-form {
  max-width: 600px;
  
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    
    .full-width {
      grid-column: span 2;
    }
  }
  
  &.password-form {
    max-width: 400px;
  }
}

.avatar-uploader-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .avatar-uploader {
    position: relative;
    cursor: pointer;
    
    :deep(.el-upload) {
      border-radius: 50%;
      overflow: hidden;
      transition: all 0.3s;
      
      &:hover .hover-mask {
        opacity: 1;
      }
    }
    
    .hover-mask {
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.5);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      opacity: 0;
      transition: opacity 0.3s;
    }
  }
  
  .upload-tip {
    p {
      font-size: 12px;
      color: #94a3b8;
      margin: 2px 0;
    }
  }
}

.region-group {
  display: flex;
  gap: 12px;
  :deep(.el-select) {
    width: 100%;
  }
}

.form-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
