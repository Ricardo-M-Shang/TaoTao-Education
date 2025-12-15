<template>
  <div class="teacher-profile-page">
    <div class="container" v-loading="saving">
      <div class="header">
        <h2>资料设置</h2>
        <p class="sub">完善讲师个人信息</p>
      </div>

      <el-form :model="form" label-width="80px" class="profile-form">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <el-avatar :size="72" :src="form.avatar || defaultAvatar" />
              <div class="upload-mask">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
            <span class="tip">点击更换头像，支持jpg/png，最大2MB</span>
          </div>
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit style="max-width: 240px" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" style="max-width: 240px" />
        </el-form-item>

        <el-form-item label="签名">
          <el-input v-model="form.signature" type="textarea" :rows="3" placeholder="写一句话介绍自己吧" maxlength="100" show-word-limit style="max-width: 360px" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, UploadRawFile } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateUserInfo } from '@/api/user'
import { uploadAvatar } from '@/api/file'

const userStore = useUserStore()
const saving = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const form = reactive({
  nickname: '',
  email: '',
  avatar: '',
  signature: ''
})

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
    form.avatar = url
    ElMessage.success('头像上传成功')
  } catch (e) {
    console.error(e)
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await updateUserInfo({
      nickname: form.nickname,
      email: form.email,
      avatar: form.avatar,
      signature: form.signature
    })
    ElMessage.success('保存成功')
    userStore.fetchUserInfo()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || ''
    form.email = userStore.userInfo.email || ''
    form.avatar = userStore.userInfo.avatar || ''
    form.signature = userStore.userInfo.signature || ''
  }
})
</script>

<style scoped lang="scss">
.teacher-profile-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 800px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { margin-bottom: 16px;
  h2 { font-size: 18px; margin-bottom: 4px; }
  .sub { font-size: 12px; color: var(--text-muted); }
}
.profile-form :deep(.el-form-item) { margin-bottom: 16px; }
.avatar-upload { display: flex; align-items: center; gap: 12px;
  .avatar-uploader { position: relative; cursor: pointer; width: 72px; height: 72px;
    :deep(.el-upload) { width: 72px; height: 72px; border-radius: 50%; overflow: hidden; }
    .upload-mask { position: absolute; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; color: #fff; opacity: 0; transition: opacity 0.2s; }
    &:hover .upload-mask { opacity: 1; }
  }
  .tip { font-size: 10px; color: var(--text-muted); }
}
</style>

