<template>
  <div class="user-page">
    <div class="container">
      <div class="user-header">
        <el-avatar :size="52" :src="userStore.userInfo?.avatar || defaultAvatar" />
        <div class="info">
          <h2>{{ userStore.userInfo?.nickname || '用户' }}</h2>
          <p>{{ userStore.userInfo?.signature || '这个人很懒，什么都没写~' }}</p>
        </div>
        <div class="header-actions">
          <el-button size="small" round @click="$router.push('/user/statistics')">
            <el-icon><DataAnalysis /></el-icon>
            学习统计
          </el-button>
        </div>
      </div>

      <!-- 学习概览卡片 -->
      <div class="stat-overview">
        <div class="stat-item" @click="$router.push('/user/courses')">
          <div class="stat-icon courses"><el-icon><Reading /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalCourses }}</span>
            <span class="stat-label">我的课程</span>
          </div>
        </div>
        <div class="stat-item" @click="$router.push('/user/statistics')">
          <div class="stat-icon study-time"><el-icon><Timer /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ formatStudyTime(stats.totalStudyTime) }}</span>
            <span class="stat-label">学习时长</span>
          </div>
        </div>
        <div class="stat-item" @click="$router.push('/user/favorites')">
          <div class="stat-icon favorites"><el-icon><Star /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.favorites }}</span>
            <span class="stat-label">我的收藏</span>
          </div>
        </div>
        <div class="stat-item" @click="$router.push('/user/orders')">
          <div class="stat-icon orders"><el-icon><Tickets /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.orders }}</span>
            <span class="stat-label">我的订单</span>
          </div>
        </div>
      </div>

      <div class="user-body">
        <el-menu :default-active="activeMenu" class="menu" @select="handleMenuSelect">
          <el-menu-item index="profile">个人资料</el-menu-item>
          <el-menu-item index="password">修改密码</el-menu-item>
        </el-menu>
        <div class="content">
          <!-- 个人资料 -->
          <div v-if="activeMenu === 'profile'" class="section">
            <h3>个人资料</h3>
            <el-form :model="profileForm" label-width="70px" class="profile-form">
              <!-- 头像上传 -->
              <el-form-item label="头像">
                <div class="avatar-upload">
                  <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                  >
                    <el-avatar :size="64" :src="profileForm.avatar || defaultAvatar" />
                    <div class="upload-mask">
                      <el-icon><Camera /></el-icon>
                    </div>
                  </el-upload>
                  <span class="tip">点击更换头像，支持jpg/png，最大2MB</span>
                </div>
              </el-form-item>

              <el-form-item label="昵称">
                <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit size="small" style="max-width: 220px" />
              </el-form-item>

              <el-form-item label="性别">
                <el-radio-group v-model="profileForm.gender" size="small">
                  <el-radio :value="0">保密</el-radio>
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="2">女</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" size="small" style="max-width: 220px" />
              </el-form-item>

              <el-form-item label="地区">
                <div class="region-select">
                  <el-select v-model="profileForm.province" placeholder="省份" size="small" @change="handleProvinceChange">
                    <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
                  </el-select>
                  <el-select v-model="profileForm.city" placeholder="城市" size="small">
                    <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
                  </el-select>
                </div>
              </el-form-item>

              <el-form-item label="签名">
                <el-input v-model="profileForm.signature" type="textarea" :rows="2" placeholder="写一句话介绍自己吧" maxlength="100" show-word-limit style="max-width: 300px" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" size="small" :loading="saving" @click="saveProfile">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 修改密码 -->
          <div v-if="activeMenu === 'password'" class="section">
            <h3>修改密码</h3>
            <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="70px" style="max-width: 300px">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password size="small" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password size="small" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password size="small" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="changePwd">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules, UploadRawFile } from 'element-plus'
import { Camera, DataAnalysis, Reading, Timer, Star, Tickets } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, changePassword } from '@/api/user'
import { uploadAvatar } from '@/api/file'
import { getStudyStatistics } from '@/api/study'
import { getFavoriteList } from '@/api/favorite'
import { getOrderList } from '@/api/order'

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
    await updateUserInfo({
      nickname: profileForm.nickname,
      email: profileForm.email,
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
.user-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 900px; margin: 0 auto; padding: 0 16px; }

.user-header {
  background: #fff; padding: 20px; border-radius: 10px; display: flex; align-items: center; gap: 14px; margin-bottom: 16px;
  
  .info { flex: 1; }
  h2 { font-size: 16px; margin-bottom: 4px; }
  p { font-size: 11px; color: var(--text-muted); max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  
  .header-actions {
    .el-button {
      .el-icon { margin-right: 4px; }
    }
  }
}

.stat-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 16px;
  
  .stat-item {
    background: #fff;
    border-radius: 10px;
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
    
    .stat-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      color: #fff;
      
      &.courses { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
      &.study-time { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
      &.favorites { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
      &.orders { background: linear-gradient(135deg, #10b981, #34d399); }
    }
    
    .stat-info {
      display: flex;
      flex-direction: column;
      
      .stat-value {
        font-size: 18px;
        font-weight: 700;
        color: var(--text-primary);
      }
      
      .stat-label {
        font-size: 11px;
        color: var(--text-muted);
      }
    }
  }
}

.user-body { display: flex; gap: 16px; }
.menu { width: 160px; border-radius: 10px; flex-shrink: 0; :deep(.el-menu-item) { font-size: 12px; height: 40px; line-height: 40px; } }
.content { flex: 1; background: #fff; border-radius: 10px; padding: 16px; }
.section h3 { font-size: 13px; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid #eee; }

.profile-form {
  :deep(.el-form-item) { margin-bottom: 16px; }
  :deep(.el-form-item__label) { font-size: 12px; }
}

.avatar-upload {
  display: flex; align-items: center; gap: 14px;
  
  .avatar-uploader {
    position: relative; cursor: pointer; width: 64px; height: 64px;
    
    :deep(.el-upload) {
      width: 64px; height: 64px; border-radius: 50%; overflow: hidden;
    }
    
    .upload-mask {
      position: absolute; top: 0; left: 0; width: 64px; height: 64px; border-radius: 50%; 
      background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center;
      opacity: 0; transition: opacity 0.2s; color: #fff; font-size: 18px;
    }
    
    &:hover .upload-mask { opacity: 1; }
  }
  
  .tip { font-size: 10px; color: var(--text-muted); }
}

.region-select {
  display: flex; gap: 10px;
  :deep(.el-select) { width: 120px; }
}
</style>
