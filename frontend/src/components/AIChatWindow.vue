<template>
  <el-dialog
    v-model="visible"
    :title="null"
    width="480px"
    :close-on-click-modal="false"
    class="ai-chat-window"
    :class="{ 'teacher-theme': isTeacher }"
    append-to-body
    @opened="scrollToBottom"
    :show-close="false"
  >
    <div class="window-header">
      <div class="header-left">
        <div class="avatar-icon">
          <el-icon :size="20"><Service /></el-icon>
        </div>
        <div class="header-info">
          <h3>{{ isTeacher ? 'AI 教学助手' : 'AI 学习助手' }}</h3>
          <span class="status-dot">在线</span>
        </div>
      </div>
      <el-button link class="close-btn" @click="visible = false">
        <el-icon :size="20"><Close /></el-icon>
      </el-button>
    </div>

    <div class="chat-container">
      <div class="message-list" ref="messageListRef">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <p v-if="isTeacher">老师您好，我是您的AI教学助手<br>需要备课灵感或教学建议吗？</p>
          <p v-else>Hi，我是你的专属学习助手<br>有什么我可以帮你的吗？</p>
        </div>
        
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message-item"
          :class="msg.role"
        >
          <div class="avatar">
            <el-icon v-if="msg.role === 'ai'"><Service /></el-icon>
            <el-icon v-else-if="isTeacher"><Monitor /></el-icon>
            <el-icon v-else><User /></el-icon>
          </div>
          <div class="content-wrapper">
            <div class="bubble" v-html="formatContent(msg.content)"></div>
            <div class="time" v-if="msg.time">{{ msg.time }}</div>
          </div>
        </div>
        
        <div v-if="loading" class="message-item ai">
          <div class="avatar">
            <el-icon><Service /></el-icon>
          </div>
          <div class="content-wrapper">
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
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="输入消息..."
          resize="none"
          class="chat-input"
          @keydown.enter.prevent="sendMessage"
        />
        <el-button 
          type="primary" 
          class="send-btn" 
          @click="sendMessage" 
          :loading="loading"
          :disabled="!inputMessage.trim()"
          circle
        >
          <el-icon><Position /></el-icon>
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'
import { chatWithAI } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { 
  Service, User, Monitor, Position, 
  Close, ChatDotRound 
} from '@element-plus/icons-vue'

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
  // 优化列表显示
  safeText = safeText.replace(/^\s*[-•]\s+(.*)$/gm, '<div class="list-item"><span class="bullet">•</span><span>$1</span></div>')
  safeText = safeText.replace(/^\s*(\d+\.)\s+(.*)$/gm, '<div class="list-item"><span class="num">$1</span><span>$2</span></div>')
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
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  
  .el-dialog__header {
    display: none; // 隐藏默认头部
  }
  
  .el-dialog__body {
    padding: 0;
  }
}

// 教师主题
.ai-chat-window.teacher-theme {
  .window-header {
    background: rgba(255, 255, 255, 0.9);
    
    .avatar-icon {
      background: linear-gradient(135deg, #f97316, #ea580c);
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
    }
  }
  
  .message-item.user .bubble {
    background: linear-gradient(135deg, #f97316, #ea580c);
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.2);
  }
  
  .send-btn {
    background: linear-gradient(135deg, #f97316, #ea580c) !important;
    border: none;
    &:hover { opacity: 0.9; }
  }
}
</style>

<style lang="scss" scoped>
.window-header {
  height: 64px;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .avatar-icon {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }
    
    .header-info {
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
        line-height: 1.2;
      }
      
      .status-dot {
        font-size: 12px;
        color: #10b981;
        display: flex;
        align-items: center;
        gap: 4px;
        
        &::before {
          content: '';
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: currentColor;
        }
      }
    }
  }
  
  .close-btn {
    color: #94a3b8;
    &:hover { color: #64748b; background: rgba(0,0,0,0.05); }
  }
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
  background: #f8fafc;
  padding-top: 64px;
  
  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    scroll-behavior: smooth;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(0,0,0,0.1);
      border-radius: 3px;
    }
    
    .empty-state {
      text-align: center;
      padding-top: 80px;
      color: #94a3b8;
      
      .empty-icon {
        width: 80px;
        height: 80px;
        background: white;
        border-radius: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 24px;
        font-size: 40px;
        color: #cbd5e1;
        box-shadow: 0 20px 40px -10px rgba(0,0,0,0.05);
      }
      
      p {
        font-size: 14px;
        line-height: 1.6;
      }
    }
    
    .message-item {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      animation: slideIn 0.3s ease-out;
      
      .avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        font-size: 18px;
        background: white;
        border: 1px solid #e2e8f0;
        color: #64748b;
      }
      
      .content-wrapper {
        display: flex;
        flex-direction: column;
        max-width: 75%;
        gap: 4px;
        
        .bubble {
          padding: 12px 16px;
          border-radius: 16px;
          font-size: 14px;
          line-height: 1.6;
          position: relative;
          word-break: break-word;
          
          :deep(.list-item) {
            display: flex;
            gap: 8px;
            margin-bottom: 4px;
            .bullet, .num { font-weight: bold; flex-shrink: 0; }
          }
          
          :deep(strong) { font-weight: 600; }
        }
        
        .time {
          font-size: 11px;
          color: #94a3b8;
          margin: 0 4px;
        }
      }
      
      &.user {
        flex-direction: row-reverse;
        
        .avatar {
          background: #3b82f6;
          color: white;
          border: none;
        }
        
        .content-wrapper {
          align-items: flex-end;
          
          .bubble {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            color: white;
            border-radius: 16px 16px 4px 16px;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
          }
        }
      }
      
      &.ai {
        .content-wrapper {
          align-items: flex-start;
          
          .bubble {
            background: white;
            color: #1e293b;
            border-radius: 4px 16px 16px 16px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            border: 1px solid rgba(226, 232, 240, 0.6);
          }
        }
      }
    }
  }
  
  .input-area {
    padding: 16px 20px;
    background: white;
    border-top: 1px solid #f1f5f9;
    display: flex;
    gap: 12px;
    align-items: flex-end;
    
    .chat-input {
      :deep(.el-textarea__inner) {
        background: #f8fafc;
        border: none;
        box-shadow: none;
        padding: 12px;
        border-radius: 12px;
        font-size: 14px;
        transition: all 0.2s;
        
        &:focus {
          background: white;
          box-shadow: 0 0 0 2px #e2e8f0 inset;
        }
      }
    }
    
    .send-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      padding: 0;
      flex-shrink: 0;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      border: none;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
      transition: all 0.2s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
      }
      
      &:active { transform: translateY(0); }
      
      &:disabled {
        background: #e2e8f0;
        box-shadow: none;
        transform: none;
        cursor: not-allowed;
      }
    }
  }
}

.typing {
  display: flex;
  gap: 4px;
  padding: 16px 20px !important;
  
  .dot {
    width: 6px;
    height: 6px;
    background: #cbd5e1;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>