<template>
  <el-dialog
    v-model="visible"
    :title="isTeacher ? 'AI 教学助手' : 'AI 学习助手'"
    width="500px"
    :close-on-click-modal="false"
    class="ai-chat-window"
    :class="{ 'teacher-theme': isTeacher }"
    append-to-body
    @opened="scrollToBottom"
  >
    <div class="chat-container">
      <div class="message-list" ref="messageListRef">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="icon">🤖</div>
          <p v-if="isTeacher">老师您好，我是您的AI教学助手！<br>需要备课灵感或教学建议吗？</p>
          <p v-else>Hi，我是你的专属学习助手！<br>有什么我可以帮你的吗？</p>
        </div>
        
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message-item"
          :class="msg.role"
        >
          <div class="avatar">
            {{ msg.role === 'user' ? (isTeacher ? '👨‍🏫' : '👤') : 'AI' }}
          </div>
          <div class="content">
            <div class="bubble" v-html="formatContent(msg.content)"></div>
            <div class="time" v-if="msg.time">{{ msg.time }}</div>
          </div>
        </div>
        
        <div v-if="loading" class="message-item ai">
          <div class="avatar">AI</div>
          <div class="content">
            <div class="bubble typing">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="请输入你的问题..."
          resize="none"
          @keydown.enter.prevent="sendMessage"
        />
        <div class="actions">
          <el-button type="primary" size="small" @click="sendMessage" :loading="loading">发送</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'
import { chatWithAI } from '@/api/ai'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  initialQuery?: string
  role?: number // 1-学生, 2-老师
}>()

const emit = defineEmits(['update:modelValue'])

const currentRole = computed(() => props.role || 1)
const isTeacher = computed(() => currentRole.value === 2)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

interface Message {
  role: 'user' | 'ai'
  content: string
  time?: string
}

// 分别存储学生和老师的聊天记录
const studentMessages = ref<Message[]>([])
const teacherMessages = ref<Message[]>([])

const messages = computed({
  get: () => isTeacher.value ? teacherMessages.value : studentMessages.value,
  set: (val) => {
    if (isTeacher.value) teacherMessages.value = val
    else studentMessages.value = val
  }
})

const inputMessage = ref('')
const loading = ref(false)
const messageListRef = ref<HTMLElement | null>(null)

// 监听初始查询
watch(() => props.modelValue, (val) => {
  if (val && props.initialQuery) {
    inputMessage.value = props.initialQuery
    sendMessage()
  }
})

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

function formatContent(text: string): string {
  if (!text) return ''
  let safeText = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  safeText = safeText.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  safeText = safeText.replace(/^\s*[-•]\s+(.*)$/gm, '<li>$1</li>')
  safeText = safeText.replace(/^\s*(\d+\.)\s+(.*)$/gm, '<li>$2</li>')
  if (safeText.includes('<li>')) {
    safeText = safeText.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
  }
  safeText = safeText.replace(/\n/g, '<br/>')
  return safeText
}

async function sendMessage() {
  const text = inputMessage.value.trim()
  if (!text || loading.value) return
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: text,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  })
  
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()
  
  try {
    const res = await chatWithAI({
      message: text,
      role: currentRole.value // 使用当前角色
    })
    
    // 添加AI消息
    messages.value.push({
      role: 'ai',
      content: res.data.reply,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    })
  } catch (e) {
    ElMessage.error('AI助手暂时开小差了，请稍后再试')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style lang="scss">
.ai-chat-window {
  border-radius: 16px;
  overflow: hidden;
  
  .el-dialog__header {
    margin: 0;
    padding: 16px 20px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
    
    .el-dialog__title {
      font-size: 16px;
      font-weight: 600;
      color: #0f172a;
    }
  }
  
  .el-dialog__body {
    padding: 0;
  }
}

.ai-chat-window.teacher-theme {
  .el-button--primary {
    background-color: #f97316;
    border-color: #f97316;
    &:hover { background-color: #ea580c; border-color: #ea580c; }
  }
}
</style>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 500px;
  background: #fff;
  
  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background: #f8fafc;
    
    .empty-state {
      text-align: center;
      padding-top: 60px;
      color: #94a3b8;
      
      .icon {
        font-size: 48px;
        margin-bottom: 16px;
      }
      p { line-height: 1.6; }
    }
    
    .message-item {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
      
      &.user {
        flex-direction: row-reverse;
        
        .avatar { background: #dbeafe; color: #2563eb; }
        .content {
          align-items: flex-end;
          .bubble {
            background: #3b82f6;
            color: #fff;
            border-radius: 12px 12px 0 12px;
          }
        }
      }
      
      &.ai {
        .avatar { background: #fff; color: #3b82f6; border: 1px solid #e2e8f0; }
        .content {
          align-items: flex-start;
          .bubble {
            background: #fff;
            color: #334155;
            border: 1px solid #e2e8f0;
            border-radius: 12px 12px 12px 0;
          }
        }
      }
    }

    // 教师主题样式覆盖
  }
  
  .input-area {
    padding: 16px;
    background: #fff;
    border-top: 1px solid #e2e8f0;
    
    :deep(.el-textarea__inner) {
      box-shadow: none;
      background: #f8fafc;
      padding: 10px;
      border-radius: 8px;
      
      &:focus { background: #fff; box-shadow: 0 0 0 1px #3b82f6 inset; }
    }
    
    .actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 8px;
    }
  }
}

// 提升到全局或父级选择器处理教师主题
.ai-chat-window.teacher-theme {
  .message-item.user {
    .avatar { background: #ffedd5 !important; color: #ea580c !important; }
    .content .bubble { background: #f97316 !important; }
  }
  .message-item.ai {
    .avatar { color: #f97316 !important; }
  }
  .input-area .el-textarea__inner:focus {
    box-shadow: 0 0 0 1px #f97316 inset !important;
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>