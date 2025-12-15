<template>
  <div class="chat-rooms">
    <!-- 顶部Tab切换 -->
    <div class="header">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我的聊天室" name="joined" />
        <el-tab-pane label="我创建的" name="created" v-if="userRole === 2" />
      </el-tabs>
      <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="invitation-badge">
        <el-button type="primary" text @click="showInvitations = true">
          <el-icon><Bell /></el-icon>
          邀请消息
        </el-button>
      </el-badge>
    </div>

    <!-- 聊天室列表 -->
    <div class="room-list" v-loading="loading">
      <el-empty v-if="rooms.length === 0" description="暂无聊天室" />
      
      <div 
        v-for="room in rooms" 
        :key="room.id" 
        class="room-card"
        :class="{ 'is-pinned': room.isPinned }"
        @click="enterRoom(room)"
      >
        <div class="room-avatar">
          <el-avatar :size="56" :src="room.cover || room.creatorAvatar">
            {{ room.name.charAt(0) }}
          </el-avatar>
          <el-badge v-if="room.unreadCount > 0" :value="room.unreadCount" class="unread-badge" />
        </div>
        
        <div class="room-info">
          <div class="room-header">
            <span class="room-name">
              <el-icon v-if="room.isPinned"><Top /></el-icon>
              {{ room.name }}
            </span>
            <span class="room-time">{{ formatTime(room.latestMessageTime) }}</span>
          </div>
          <div class="room-meta">
            <span class="course-tag">
              <el-icon><VideoCamera /></el-icon>
              {{ room.courseTitle }}
            </span>
          </div>
          <div class="room-message">
            {{ room.latestMessage || '暂无消息' }}
          </div>
        </div>
        
        <div class="room-actions" @click.stop>
          <el-dropdown trigger="click">
            <el-button type="info" text circle>
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="togglePin(room)">
                  {{ room.isPinned ? '取消置顶' : '置顶' }}
                </el-dropdown-item>
                <el-dropdown-item v-if="!room.isCreator" @click="handleLeave(room)">
                  退出聊天室
                </el-dropdown-item>
                <el-dropdown-item v-if="room.isCreator" @click="handleManage(room)">
                  管理聊天室
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 创建聊天室按钮（仅讲师） -->
    <el-button 
      v-if="userRole === 2" 
      class="create-btn"
      type="primary" 
      circle 
      size="large"
      @click="showCreateDialog = true"
    >
      <el-icon :size="24"><Plus /></el-icon>
    </el-button>

    <!-- 创建聊天室对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建聊天室" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="聊天室名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入聊天室名称" />
        </el-form-item>
        <el-form-item label="关联课程" prop="courseId">
          <el-select v-model="createForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option 
              v-for="course in myCourses" 
              :key="course.id" 
              :label="course.title" 
              :value="Number(course.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入聊天室描述" />
        </el-form-item>
        <el-form-item label="公告">
          <el-input v-model="createForm.announcement" type="textarea" :rows="2" placeholder="请输入聊天室公告" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>

    <!-- 邀请消息抽屉 -->
    <el-drawer v-model="showInvitations" title="聊天室邀请" size="400px">
      <div class="invitation-list">
        <el-empty v-if="invitations.length === 0" description="暂无邀请" />
        
        <div v-for="inv in invitations" :key="inv.id" class="invitation-card">
          <div class="invitation-header">
            <el-avatar :size="40" :src="inv.inviterAvatar">{{ inv.inviterName?.charAt(0) }}</el-avatar>
            <div class="invitation-info">
              <div class="inviter-name">{{ inv.inviterName }}</div>
              <div class="invite-time">{{ formatTime(inv.createTime) }}</div>
            </div>
          </div>
          <div class="invitation-content">
            邀请您加入聊天室「<strong>{{ inv.roomName }}</strong>」
            <div v-if="inv.message" class="invite-message">{{ inv.message }}</div>
          </div>
          <div class="invitation-actions">
            <el-button type="primary" size="small" @click="handleAccept(inv)">接受</el-button>
            <el-button size="small" @click="handleReject(inv)">拒绝</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Bell, Top, VideoCamera, MoreFilled, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getMyChatRooms,
  getCreatedChatRooms,
  createChatRoom,
  leaveChatRoom,
  updateChatMemberSettings,
  getPendingInvitations,
  getPendingInvitationCount,
  acceptInvitation,
  rejectInvitation,
  type ChatRoom,
  type ChatInvitation,
  type CreateRoomParams
} from '@/api/chat'

const router = useRouter()
const userStore = useUserStore()
const userRole = computed(() => userStore.userInfo?.role || 1)

const activeTab = ref('joined')
const loading = ref(false)
const rooms = ref<ChatRoom[]>([])
const invitations = ref<ChatInvitation[]>([])
const pendingCount = ref(0)
const showInvitations = ref(false)
const showCreateDialog = ref(false)
const creating = ref(false)
const createFormRef = ref<FormInstance>()

const createForm = ref<CreateRoomParams>({
  name: '',
  courseId: undefined as any,
  description: '',
  announcement: ''
})

const createRules = {
  name: [{ required: true, message: '请输入聊天室名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择关联课程', trigger: 'change' }]
}

// 我的课程（讲师创建的）
const myCourses = ref<{ id: number; title: string }[]>([])

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return date.toLocaleDateString()
}

const loadRooms = async () => {
  loading.value = true
  try {
    const res = activeTab.value === 'joined' 
      ? await getMyChatRooms()
      : await getCreatedChatRooms()
    if (res.code == 200) {
      rooms.value = res.data || []
    }
  } catch (error) {
    console.error('加载聊天室失败:', error)
  } finally {
    loading.value = false
  }
}

const loadInvitations = async () => {
  try {
    console.log('=== 加载邀请 ===')
    console.log('当前用户ID:', userStore.userInfo?.id)
    const [invRes, countRes] = await Promise.all([
      getPendingInvitations(),
      getPendingInvitationCount()
    ])
    console.log('邀请列表响应:', invRes)
    console.log('邀请数量响应:', countRes)
    if (invRes.code == 200) {
      invitations.value = invRes.data || []
      console.log('待处理邀请:', invitations.value)
    }
    if (countRes.code == 200) {
      pendingCount.value = Number(countRes.data) || 0
      console.log('待处理数量:', pendingCount.value)
    }
  } catch (error) {
    console.error('加载邀请失败:', error)
  }
}

const loadMyCourses = async () => {
  // 角色2是讲师
  console.log('=== loadMyCourses 开始 ===')
  console.log('userStore.userInfo:', userStore.userInfo)
  console.log('userRole.value:', userRole.value)
  
  // 无论角色如何都尝试加载（调试用）
  try {
    console.log('正在请求 /api/course/teacher/list ...')
    const res = await fetch('/api/course/teacher/list', {
      headers: { 
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      }
    })
    console.log('响应状态:', res.status, res.statusText)
    const data = await res.json()
    console.log('讲师课程列表响应:', JSON.stringify(data, null, 2))
    // 注意：后端返回的 code 可能是字符串 "200" 或数字 200
    if (data.code == 200) {
      myCourses.value = data.data?.records || []
      console.log('加载的课程数量:', myCourses.value.length)
      console.log('课程列表:', myCourses.value)
    } else {
      console.error('获取课程失败:', data.message)
      ElMessage.error('获取课程失败: ' + data.message)
    }
  } catch (error) {
    console.error('加载课程失败:', error)
  }
  console.log('=== loadMyCourses 结束 ===')
}

const handleTabChange = () => {
  loadRooms()
}

const enterRoom = (room: ChatRoom) => {
  router.push(`/chat/room/${room.id}`)
}

const togglePin = async (room: ChatRoom) => {
  try {
    await updateChatMemberSettings(room.id, !room.isPinned)
    room.isPinned = room.isPinned ? 0 : 1
    ElMessage.success(room.isPinned ? '已置顶' : '已取消置顶')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleLeave = async (room: ChatRoom) => {
  try {
    await ElMessageBox.confirm('确定要退出该聊天室吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await leaveChatRoom(room.id)
    ElMessage.success('已退出聊天室')
    loadRooms()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleManage = (room: ChatRoom) => {
  router.push(`/chat/manage/${room.id}`)
}

const handleCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  
  creating.value = true
  try {
    // 确保 courseId 是数字类型
    const data = {
      ...createForm.value,
      courseId: Number(createForm.value.courseId)
    }
    const res = await createChatRoom(data)
    if (res.code == 200) {
      ElMessage.success('创建成功')
      showCreateDialog.value = false
      createForm.value = { name: '', courseId: undefined as any, description: '', announcement: '' }
      loadRooms()
    }
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

const handleAccept = async (inv: ChatInvitation) => {
  try {
    await acceptInvitation(inv.id)
    ElMessage.success('已加入聊天室')
    loadInvitations()
    loadRooms()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (inv: ChatInvitation) => {
  try {
    await rejectInvitation(inv.id)
    ElMessage.success('已拒绝邀请')
    loadInvitations()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(async () => {
  // 确保用户信息已加载（页面刷新后 userInfo 可能为空）
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  loadRooms()
  loadInvitations()
  loadMyCourses()
})
</script>

<style lang="scss" scoped>
.chat-rooms {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
  position: relative;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .invitation-badge {
    :deep(.el-badge__content) {
      top: 8px;
      right: 8px;
    }
  }
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.room-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  &.is-pinned {
    background: linear-gradient(135deg, #f0f9ff 0%, #fff 100%);
    border-left: 3px solid var(--el-color-primary);
  }
}

.room-avatar {
  position: relative;
  
  .unread-badge {
    position: absolute;
    top: -4px;
    right: -4px;
  }
}

.room-info {
  flex: 1;
  min-width: 0;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.room-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 4px;
  
  .el-icon {
    color: var(--el-color-primary);
    font-size: 14px;
  }
}

.room-time {
  font-size: 12px;
  color: #999;
}

.room-meta {
  margin-bottom: 6px;
}

.course-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.room-message {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.create-btn {
  position: fixed;
  right: 40px;
  bottom: 40px;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.invitation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.invitation-card {
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.invitation-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.invitation-info {
  .inviter-name {
    font-weight: 600;
    color: #333;
  }
  .invite-time {
    font-size: 12px;
    color: #999;
  }
}

.invitation-content {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  
  .invite-message {
    margin-top: 8px;
    padding: 8px;
    background: #fff;
    border-radius: 4px;
    font-size: 13px;
  }
}

.invitation-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>

