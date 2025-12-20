<template>
  <div class="chat-rooms-page">
    <div class="container">
      <!-- 头部区域 -->
      <div class="page-header">
        <div class="tabs-wrapper">
          <div 
            class="tab-item" 
            :class="{ active: activeTab === 'joined' }"
            @click="switchTab('joined')"
          >
            我的聊天室
          </div>
          <div 
            v-if="userRole === 2"
            class="tab-item" 
            :class="{ active: activeTab === 'created' }"
            @click="switchTab('created')"
          >
            我创建的
          </div>
        </div>
        
        <div class="header-actions">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="notification-badge">
            <el-button class="icon-btn" circle @click="showInvitations = true">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
        </div>
      </div>

      <!-- 列表区域 -->
      <div class="room-grid" v-loading="loading">
        <el-empty v-if="!loading && rooms.length === 0" description="暂无聊天室" />
        
        <div 
          v-for="(room, index) in rooms" 
          :key="room.id" 
          class="room-card"
          :class="{ 'is-pinned': room.isPinned }"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="enterRoom(room)"
        >
          <div class="card-content">
            <div class="avatar-wrapper">
              <el-avatar :size="56" :src="room.cover || room.creatorAvatar" shape="square">
                {{ room.name.charAt(0) }}
              </el-avatar>
              <div v-if="room.unreadCount > 0" class="unread-dot">{{ room.unreadCount > 99 ? '99+' : room.unreadCount }}</div>
            </div>
            
            <div class="info-wrapper">
              <div class="top-row">
                <h3 class="room-name">
                  <el-icon v-if="room.isPinned" class="pin-icon"><Top /></el-icon>
                  {{ room.name }}
                </h3>
                <span class="time">{{ formatTime(room.latestMessageTime) }}</span>
              </div>
              
              <div class="course-badge">
                <el-icon><Collection /></el-icon>
                {{ room.courseTitle }}
              </div>
              
              <div class="message-preview">
                {{ room.latestMessage || '暂无消息' }}
              </div>
            </div>
          </div>

          <!-- 操作菜单 -->
          <div class="card-actions" @click.stop>
            <el-dropdown trigger="click">
              <div class="more-btn"><el-icon><MoreFilled /></el-icon></div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="togglePin(room)">
                    {{ room.isPinned ? '取消置顶' : '置顶聊天' }}
                  </el-dropdown-item>
                  <el-dropdown-item v-if="room.isCreator" @click="handleManage(room)">
                    管理聊天室
                  </el-dropdown-item>
                  <el-dropdown-item v-else @click="handleLeave(room)" divided>
                    <span class="danger-text">退出聊天室</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 悬浮创建按钮 -->
    <div v-if="userRole === 2" class="fab-container">
      <el-tooltip content="创建聊天室" placement="left">
        <button class="fab-btn" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
        </button>
      </el-tooltip>
    </div>

    <!-- 创建对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="创建新聊天室" 
      width="480px"
      class="custom-dialog"
      align-center
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-position="top">
        <el-form-item label="聊天室名称" prop="name">
          <el-input v-model="createForm.name" placeholder="给聊天室起个好听的名字" size="large" />
        </el-form-item>
        <el-form-item label="关联课程" prop="courseId">
          <el-select v-model="createForm.courseId" placeholder="选择关联的课程" style="width: 100%" size="large">
            <el-option 
              v-for="course in myCourses" 
              :key="course.id" 
              :label="course.title" 
              :value="Number(course.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="createForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="简单介绍一下这个聊天室..." 
            resize="none"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreate" :loading="creating">立即创建</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 邀请抽屉 -->
    <el-drawer v-model="showInvitations" title="收到的邀请" size="380px" class="custom-drawer">
      <div class="invitation-list">
        <el-empty v-if="invitations.length === 0" description="暂无新邀请" :image-size="120" />
        
        <div v-for="inv in invitations" :key="inv.id" class="invitation-item">
          <div class="inv-header">
            <div class="inviter">
              <el-avatar :size="32" :src="inv.inviterAvatar">{{ inv.inviterName?.charAt(0) }}</el-avatar>
              <span class="name">{{ inv.inviterName }}</span>
              <span class="action">邀请你加入</span>
            </div>
            <span class="time">{{ formatTime(inv.createTime) }}</span>
          </div>
          
          <div class="room-preview">
            <div class="room-icon"><el-icon><ChatDotSquare /></el-icon></div>
            <span class="room-name">{{ inv.roomName }}</span>
          </div>
          
          <div class="inv-actions">
            <el-button size="small" @click="handleReject(inv)">忽略</el-button>
            <el-button type="primary" size="small" @click="handleAccept(inv)">接受邀请</el-button>
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
import { Bell, Top, Collection, MoreFilled, Plus, ChatDotSquare } from '@element-plus/icons-vue'
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

const myCourses = ref<{ id: number; title: string }[]>([])

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
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

const switchTab = (tab: string) => {
  activeTab.value = tab
  loadRooms()
}

const loadInvitations = async () => {
  try {
    const [invRes, countRes] = await Promise.all([
      getPendingInvitations(),
      getPendingInvitationCount()
    ])
    if (invRes.code == 200) invitations.value = invRes.data || []
    if (countRes.code == 200) pendingCount.value = Number(countRes.data) || 0
  } catch (error) {
    console.error(error)
  }
}

const loadMyCourses = async () => {
  if (userRole.value !== 2) return
  try {
    const res = await fetch('/api/course/teacher/list', {
      headers: { 
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      }
    })
    const data = await res.json()
    if (data.code == 200) {
      myCourses.value = data.data?.records || []
    }
  } catch (error) {
    console.error(error)
  }
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
    if (error !== 'cancel') ElMessage.error('操作失败')
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
  if (!userStore.userInfo) await userStore.fetchUserInfo()
  loadRooms()
  loadInvitations()
  loadMyCourses()
})
</script>

<style lang="scss" scoped>
.chat-rooms-page {
  min-height: calc(100vh - 60px);
  background-color: #f3f4f6;
  padding: 24px 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Header & Tabs */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.tabs-wrapper {
  display: flex;
  background: #fff;
  padding: 4px;
  border-radius: 99px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .tab-item {
    padding: 8px 24px;
    border-radius: 99px;
    font-size: 14px;
    font-weight: 500;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      color: #1f2937;
    }
    
    &.active {
      background: #4f46e5;
      color: #fff;
      box-shadow: 0 2px 8px rgba(79, 70, 229, 0.3);
    }
  }
}

.icon-btn {
  border: none;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  color: #6b7280;
  
  &:hover {
    color: #4f46e5;
    background: #eef2ff;
  }
}

/* Grid Layout */
.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.room-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  cursor: pointer;
  position: relative;
  animation: slideUp 0.5s backwards;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.1);
    border-color: #e0e7ff;
    
    .card-actions {
      opacity: 1;
    }
  }
  
  &.is-pinned {
    background: linear-gradient(145deg, #f5f3ff 0%, #fff 100%);
    border: 1px solid #e0e7ff;
  }
  
  .card-content {
    padding: 20px;
    display: flex;
    gap: 16px;
  }
  
  .avatar-wrapper {
    position: relative;
    
    .el-avatar {
      background: #e0e7ff;
      color: #4f46e5;
      font-size: 24px;
      font-weight: 600;
      border-radius: 16px;
    }
    
    .unread-dot {
      position: absolute;
      top: -4px;
      right: -4px;
      background: #ef4444;
      color: #fff;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 99px;
      border: 2px solid #fff;
      font-weight: 600;
    }
  }
  
  .info-wrapper {
    flex: 1;
    min-width: 0;
    
    .top-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 6px;
      
      .room-name {
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
        margin: 0;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        flex: 1;
        margin-right: 8px;
        
        .pin-icon {
          color: #f59e0b;
          font-size: 14px;
          vertical-align: -1px;
        }
      }
      
      .time {
        font-size: 12px;
        color: #9ca3af;
        white-space: nowrap;
      }
    }
    
    .course-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #6b7280;
      background: #f3f4f6;
      padding: 2px 8px;
      border-radius: 6px;
      margin-bottom: 10px;
      max-width: 100%;
      
      .el-icon { font-size: 12px; }
    }
    
    .message-preview {
      font-size: 13px;
      color: #6b7280;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .card-actions {
    position: absolute;
    bottom: 12px;
    right: 12px;
    opacity: 0;
    transition: opacity 0.2s;
    
    .more-btn {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      color: #9ca3af;
      
      &:hover {
        background: #f3f4f6;
        color: #4f46e5;
      }
    }
  }
}

/* FAB */
.fab-container {
  position: fixed;
  bottom: 40px;
  right: 40px;
  z-index: 99;
  
  .fab-btn {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4f46e5, #7c3aed);
    border: none;
    color: #fff;
    font-size: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.4);
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: scale(1.1) rotate(90deg);
      box-shadow: 0 8px 24px rgba(79, 70, 229, 0.5);
    }
  }
}

/* Invitation List */
.invitation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 0;
  
  .invitation-item {
    background: #f9fafb;
    border-radius: 12px;
    padding: 16px;
    
    .inv-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      
      .inviter {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;
        
        .name { font-weight: 600; color: #1f2937; }
        .action { color: #6b7280; }
      }
      
      .time {
        font-size: 11px;
        color: #9ca3af;
      }
    }
    
    .room-preview {
      display: flex;
      align-items: center;
      gap: 10px;
      background: #fff;
      padding: 10px;
      border-radius: 8px;
      margin-bottom: 16px;
      border: 1px solid #e5e7eb;
      
      .room-icon {
        width: 32px;
        height: 32px;
        background: #eef2ff;
        color: #4f46e5;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      .room-name {
        font-weight: 500;
        color: #1f2937;
        font-size: 14px;
      }
    }
    
    .inv-actions {
      display: flex;
      gap: 12px;
      
      .el-button {
        flex: 1;
      }
    }
  }
}

.danger-text { color: #ef4444; }

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 640px) {
  .room-grid {
    grid-template-columns: 1fr;
  }
}
</style>
