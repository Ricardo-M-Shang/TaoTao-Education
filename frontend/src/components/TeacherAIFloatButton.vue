<template>
  <!-- AI 助手悬浮按钮 -->
  <div class="ai-float-btn" @click="openAiChat">
    <div class="btn-content">
      <el-icon class="icon"><ChatDotRound /></el-icon>
      <span class="text">AI助手</span>
    </div>
  </div>

  <!-- AI 聊天窗口 -->
  <AIChatWindow v-model="chatVisible" :role="2" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import AIChatWindow from '@/components/AIChatWindow.vue'

const chatVisible = ref(false)

function openAiChat() {
  chatVisible.value = true
}
</script>

<style scoped lang="scss">
.ai-float-btn {
  position: fixed;
  bottom: 40px;
  right: 40px;
  z-index: 1000;
  cursor: pointer;
  
  .btn-content {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, #f97316, #ea580c);
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(234, 88, 12, 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    color: #fff;
    
    .icon {
      font-size: 24px;
      margin-bottom: 2px;
    }
    
    .text {
      font-size: 10px;
      font-weight: 500;
    }
    
    &:hover {
      transform: scale(1.1) translateY(-5px);
      box-shadow: 0 8px 20px rgba(234, 88, 12, 0.4);
    }
    
    &::after {
      content: '';
      position: absolute;
      inset: -4px;
      border-radius: 50%;
      border: 2px solid rgba(249, 115, 22, 0.3);
      animation: pulse 2s infinite;
    }
  }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}
</style>

