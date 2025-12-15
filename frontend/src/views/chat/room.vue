<template>
  <div class="chat-room" v-loading="loading">
    <!-- 顶部栏 -->
    <div class="chat-header">
      <el-button type="info" text circle @click="goBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </el-button>
      <div class="room-title">
        <span class="name">{{ room?.name }}</span>
        <span class="member-count">{{ room?.memberCount }}人</span>
      </div>
      <el-button type="info" text circle @click="showMembers = true">
        <el-icon :size="20"><User /></el-icon>
      </el-button>
    </div>

    <!-- 公告栏 -->
    <div v-if="room?.announcement" class="announcement">
      <el-icon><Bell /></el-icon>
      <span>{{ room.announcement }}</span>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef" @scroll="handleScroll">
      <div v-if="loadingMore" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      
      <div 
        v-for="msg in messages" 
        :key="msg.id" 
        class="message-item"
        :class="{ 'is-mine': msg.isMine, 'is-system': msg.messageType == 4 }"
      >
        <!-- 系统消息 -->
        <div v-if="msg.messageType == 4" class="system-message">
          {{ msg.content }}
        </div>
        
        <!-- 普通消息 -->
        <template v-else>
          <el-avatar 
            v-if="!msg.isMine" 
            :size="36" 
            :src="msg.senderAvatar"
            class="message-avatar"
          >
            {{ msg.senderName?.charAt(0) }}
          </el-avatar>
          
          <div class="message-content">
            <div v-if="!msg.isMine" class="sender-info">
              <span class="sender-name">{{ msg.senderName }}</span>
              <el-tag v-if="msg.senderRole == 2" size="small" type="warning">讲师</el-tag>
            </div>
            
            <!-- 回复预览 -->
            <div v-if="msg.replyToContent" class="reply-preview" @click="scrollToMessage(msg.replyToId)">
              <el-icon><ChatLineSquare /></el-icon>
              {{ msg.replyToContent }}
            </div>
            
            <div class="message-bubble" @contextmenu.prevent="handleContextMenu($event, msg)">
              <!-- 文本消息 -->
              <template v-if="msg.messageType == 1">
                {{ msg.content }}
              </template>
              
              <!-- 图片消息 -->
              <template v-else-if="msg.messageType == 2">
                <el-image 
                  :src="msg.attachmentUrl" 
                  fit="cover" 
                  style="max-width: 200px; border-radius: 8px;"
                  :preview-src-list="[msg.attachmentUrl]"
                />
              </template>
              
              <!-- 文件消息 -->
              <template v-else-if="msg.messageType == 3">
                <div class="file-message">
                  <el-icon :size="24"><Document /></el-icon>
                  <div class="file-info">
                    <div class="file-name">{{ msg.attachmentName }}</div>
                    <div class="file-size">{{ formatFileSize(msg.attachmentSize) }}</div>
                  </div>
                </div>
              </template>
            </div>
            
            <div class="message-time">{{ formatTime(msg.createTime) }}</div>
          </div>
          
          <el-avatar 
            v-if="msg.isMine" 
            :size="36" 
            :src="msg.senderAvatar"
            class="message-avatar"
          >
            {{ msg.senderName?.charAt(0) }}
          </el-avatar>
        </template>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <!-- 回复预览 -->
      <div v-if="replyingTo" class="replying-bar">
        <span>回复 {{ replyingTo.senderName }}: {{ replyingTo.content?.substring(0, 30) }}...</span>
        <el-button type="info" text circle size="small" @click="replyingTo = null">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="input-row">
        <el-input 
          v-model="inputMessage" 
          type="textarea"
          :rows="2"
          placeholder="输入消息..."
          @keydown.enter.exact.prevent="sendMessage"
          resize="none"
        />
        <el-button type="primary" :disabled="!inputMessage.trim()" @click="sendMessage">
          发送
        </el-button>
      </div>
    </div>

    <!-- 成员列表抽屉 -->
    <el-drawer v-model="showMembers" title="成员列表" size="320px">
      <div class="member-list">
        <div v-for="member in members" :key="member.id" class="member-item">
          <el-avatar :size="40" :src="member.avatar">{{ member.nickname?.charAt(0) }}</el-avatar>
          <div class="member-info">
            <div class="member-name">
              {{ member.nickname }}
              <el-tag v-if="member.role == 3" size="small" type="danger">创建者</el-tag>
              <el-tag v-else-if="member.role == 2" size="small" type="warning">管理员</el-tag>
            </div>
            <div class="member-status">
              <span :class="{ 'is-online': member.isOnline }">
                {{ member.isOnline ? '在线' : '离线' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 右键菜单 -->
    <div 
      v-if="contextMenu.visible" 
      class="context-menu"
      :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
      @click.stop
    >
      <div class="menu-item" @click="handleReply">
        <el-icon><ChatLineSquare /></el-icon>
        回复
      </div>
      <div v-if="contextMenu.message?.isMine" class="menu-item" @click="handleRecall">
        <el-icon><RefreshLeft /></el-icon>
        撤回
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, User, Bell, Loading, Close, Document, 
  ChatLineSquare, RefreshLeft 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getChatRoomDetail,
  getChatMessages,
  getChatMessagesBefore,
  sendChatMessage,
  recallChatMessage,
  getChatRoomMembers,
  clearChatUnread,
  createChatWebSocket,
  type ChatRoom,
  type ChatMessage,
  type ChatMember
} from '@/api/chat'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 保持字符串形式，避免大数精度丢失
const roomId = route.params.id as string
const loading = ref(true)
const loadingMore = ref(false)
const room = ref<ChatRoom | null>(null)
const messages = ref<ChatMessage[]>([])
const members = ref<ChatMember[]>([])
const inputMessage = ref('')
const replyingTo = ref<ChatMessage | null>(null)
const showMembers = ref(false)
const messageListRef = ref<HTMLElement | null>(null)

let ws: WebSocket | null = null
let hasMore = true

const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  message: null as ChatMessage | null
})

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleString('zh-CN', { 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const formatFileSize = (size?: number) => {
  if (!size) return ''
  if (size < 1024) return size + 'B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
  return (size / 1024 / 1024).toFixed(1) + 'MB'
}

const goBack = () => {
  router.push('/chat')
}

const loadRoom = async () => {
  try {
    const res = await getChatRoomDetail(roomId)
    if (res.code == 200) {
      room.value = res.data
    }
  } catch (error) {
    console.error('加载聊天室失败:', error)
    ElMessage.error('加载聊天室失败')
  }
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getChatMessages(roomId, 1, 50)
    if (res.code == 200) {
      // 后端已经正确设置了 isMine，直接使用
      messages.value = res.data || []
      hasMore = messages.value.length >= 50
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMoreMessages = async () => {
  if (loadingMore.value || !hasMore || messages.value.length === 0) return
  
  loadingMore.value = true
  const firstMsg = messages.value[0]
  try {
    const res = await getChatMessagesBefore(roomId, firstMsg.id, 20)
    if (res.code == 200 && res.data) {
      // 后端已经正确设置了 isMine，直接使用
      messages.value = [...res.data, ...messages.value]
      hasMore = res.data.length >= 20
    }
  } catch (error) {
    console.error('加载更多消息失败:', error)
  } finally {
    loadingMore.value = false
  }
}

const loadMembers = async () => {
  try {
    const res = await getChatRoomMembers(roomId)
    if (res.code == 200) {
      members.value = res.data || []
    }
  } catch (error) {
    console.error('加载成员失败:', error)
  }
}

const connectWebSocket = () => {
  const userId = userStore.userInfo?.id
  if (!userId) return

  ws = createChatWebSocket(roomId, userId)
  
  ws.onopen = () => {
    console.log('WebSocket已连接')
  }
  
  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'message') {
        // 收到新消息
        const newMsg = data.data as ChatMessage
        // 避免重复添加（发送者会从 API 响应添加，不需要从 WebSocket 再添加）
        if (!messages.value.some(m => String(m.id) === String(newMsg.id))) {
          // 判断是否是自己发送的消息（WebSocket 广播的 isMine 为 null）
          if (newMsg.isMine === null || newMsg.isMine === undefined) {
            newMsg.isMine = String(newMsg.senderId) === String(userId)
          }
          messages.value.push(newMsg)
          nextTick(() => scrollToBottom())
        }
      } else if (data.type === 'userStatus') {
        // 用户在线状态变更
        const { userId: uid, online } = data.data
        const member = members.value.find(m => String(m.userId) === String(uid))
        if (member) {
          member.isOnline = online
        }
      }
    } catch (error) {
      console.error('解析消息失败:', error)
    }
  }
  
  ws.onclose = () => {
    console.log('WebSocket已断开')
    // 尝试重连
    setTimeout(() => {
      if (!ws || ws.readyState === WebSocket.CLOSED) {
        connectWebSocket()
      }
    }, 3000)
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket错误:', error)
  }
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) return
  
  try {
    const res = await sendChatMessage({
      roomId,
      content,
      messageType: 1,
      replyToId: replyingTo.value?.id
    })
    
    if (res.code == 200 && res.data) {
      // 添加到消息列表，标记为自己发送的消息
      const newMsg = { ...res.data, isMine: true }
      // 检查是否已存在（WebSocket 可能已经收到）
      if (!messages.value.some(m => String(m.id) === String(newMsg.id))) {
        messages.value.push(newMsg)
      }
      inputMessage.value = ''
      replyingTo.value = null
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const handleScroll = () => {
  if (messageListRef.value) {
    if (messageListRef.value.scrollTop < 100) {
      loadMoreMessages()
    }
  }
  // 隐藏右键菜单
  contextMenu.value.visible = false
}

const handleContextMenu = (e: MouseEvent, msg: ChatMessage) => {
  contextMenu.value = {
    visible: true,
    x: e.clientX,
    y: e.clientY,
    message: msg
  }
}

const handleReply = () => {
  if (contextMenu.value.message) {
    replyingTo.value = contextMenu.value.message
  }
  contextMenu.value.visible = false
}

const handleRecall = async () => {
  if (!contextMenu.value.message) return
  
  try {
    await recallChatMessage(contextMenu.value.message.id)
    // 更新本地消息状态
    const msg = messages.value.find(m => m.id === contextMenu.value.message?.id)
    if (msg) {
      msg.content = '[消息已撤回]'
      msg.messageType = 4
    }
    ElMessage.success('已撤回')
  } catch (error) {
    ElMessage.error('撤回失败')
  }
  contextMenu.value.visible = false
}

const scrollToMessage = (messageId?: number) => {
  if (!messageId) return
  const el = document.querySelector(`[data-message-id="${messageId}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

// 点击其他地方隐藏右键菜单
const hideContextMenu = () => {
  contextMenu.value.visible = false
}

onMounted(async () => {
  document.addEventListener('click', hideContextMenu)
  
  await Promise.all([loadRoom(), loadMessages(), loadMembers()])
  
  // 清空未读
  clearChatUnread(roomId)
  
  // 连接WebSocket
  connectWebSocket()
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
  if (ws) {
    ws.close()
  }
})

// 监听成员列表打开
watch(showMembers, (val) => {
  if (val) {
    loadMembers()
  }
})
</script>

<style lang="scss" scoped>
.chat-room {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background: #f5f6f7;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
  
  .room-title {
    flex: 1;
    text-align: center;
    
    .name {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
    
    .member-count {
      font-size: 12px;
      color: #999;
      margin-left: 8px;
    }
  }
}

.announcement {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fffbe6;
  color: #8c6900;
  font-size: 13px;
  
  .el-icon {
    color: #faad14;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #999;
  font-size: 13px;
}

.message-item {
  display: flex;
  gap: 10px;
  
  &.is-mine {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
    }
    
    .message-bubble {
      background: var(--el-color-primary);
      color: #fff;
    }
    
    .message-time {
      text-align: right;
    }
  }
  
  &.is-system {
    justify-content: center;
  }
}

.system-message {
  font-size: 12px;
  color: #999;
  background: #f0f0f0;
  padding: 4px 12px;
  border-radius: 12px;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.sender-info {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .sender-name {
    font-size: 12px;
    color: #666;
  }
}

.reply-preview {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  
  &:hover {
    background: #eee;
  }
}

.message-bubble {
  padding: 10px 14px;
  background: #fff;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.file-message {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .file-info {
    .file-name {
      font-size: 14px;
    }
    .file-size {
      font-size: 12px;
      color: #999;
    }
  }
}

.message-time {
  font-size: 11px;
  color: #999;
}

.input-area {
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #eee;
}

.replying-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  margin-bottom: 8px;
  background: #f5f5f5;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
}

.input-row {
  display: flex;
  gap: 12px;
  
  .el-input {
    flex: 1;
  }
  
  .el-button {
    height: auto;
    align-self: flex-end;
  }
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  
  &:hover {
    background: #f5f5f5;
  }
}

.member-info {
  flex: 1;
  
  .member-name {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
  }
  
  .member-status {
    font-size: 12px;
    color: #999;
    
    .is-online {
      color: #52c41a;
    }
  }
}

.context-menu {
  position: fixed;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  z-index: 1000;
  
  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    cursor: pointer;
    font-size: 14px;
    
    &:hover {
      background: #f5f5f5;
    }
  }
}
</style>

