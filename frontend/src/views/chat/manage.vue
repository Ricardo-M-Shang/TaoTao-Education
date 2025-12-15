<template>
  <div class="chat-manage" v-loading="loading">
    <div class="page-header">
      <el-button type="info" text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>聊天室管理</h2>
    </div>

    <div class="manage-content">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button type="primary" text @click="showEditDialog = true">编辑</el-button>
          </div>
        </template>
        <div class="room-info" v-if="room">
          <div class="info-row">
            <span class="label">聊天室名称</span>
            <span class="value">{{ room.name }}</span>
          </div>
          <div class="info-row">
            <span class="label">关联课程</span>
            <span class="value">{{ room.courseTitle }}</span>
          </div>
          <div class="info-row">
            <span class="label">成员数量</span>
            <span class="value">{{ room.memberCount }} 人</span>
          </div>
          <div class="info-row">
            <span class="label">创建时间</span>
            <span class="value">{{ formatDate(room.createTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">描述</span>
            <span class="value">{{ room.description || '暂无描述' }}</span>
          </div>
        </div>
      </el-card>

      <!-- 公告管理 -->
      <el-card class="announcement-card">
        <template #header>
          <div class="card-header">
            <span>公告管理</span>
          </div>
        </template>
        <el-input 
          v-model="announcement" 
          type="textarea" 
          :rows="3" 
          placeholder="输入聊天室公告..."
        />
        <el-button type="primary" style="margin-top: 12px;" @click="saveAnnouncement">
          保存公告
        </el-button>
      </el-card>

      <!-- 邀请学员 -->
      <el-card class="invite-card">
        <template #header>
          <div class="card-header">
            <span>邀请学员</span>
            <el-button type="primary" @click="inviteAll" :loading="invitingAll">
              邀请全部学员
            </el-button>
          </div>
        </template>
        
        <div class="student-list" v-loading="loadingStudents">
          <el-empty v-if="students.length === 0" description="暂无可邀请的学员" />
          
          <el-checkbox-group v-model="selectedStudents">
            <div v-for="student in students" :key="student.userId" class="student-item">
              <el-checkbox 
                :value="student.userId" 
                :disabled="student.inRoom || student.invited"
              >
                <div class="student-info">
                  <el-avatar :size="36" :src="student.avatar">
                    {{ student.nickname?.charAt(0) }}
                  </el-avatar>
                  <div class="student-detail">
                    <span class="name">{{ student.nickname }}</span>
                    <span class="status">
                      <el-tag v-if="student.inRoom" size="small" type="success">已加入</el-tag>
                      <el-tag v-else-if="student.invited" size="small" type="warning">已邀请</el-tag>
                    </span>
                  </div>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
          
          <el-button 
            v-if="students.some(s => !s.inRoom && !s.invited)"
            type="primary" 
            style="margin-top: 16px;"
            :disabled="selectedStudents.length === 0"
            @click="inviteSelected"
            :loading="inviting"
          >
            邀请选中学员 ({{ selectedStudents.length }})
          </el-button>
        </div>
      </el-card>

      <!-- 成员管理 -->
      <el-card class="member-card">
        <template #header>
          <div class="card-header">
            <span>成员管理</span>
          </div>
        </template>
        
        <el-table :data="members" style="width: 100%">
          <el-table-column label="成员">
            <template #default="{ row }">
              <div class="member-cell">
                <el-avatar :size="36" :src="row.avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
                <span class="name">{{ row.nickname }}</span>
                <el-tag v-if="row.role === 3" size="small" type="danger">创建者</el-tag>
                <el-tag v-else-if="row.role === 2" size="small" type="warning">管理员</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 2" type="danger">已禁言</el-tag>
              <el-tag v-else :type="row.isOnline ? 'success' : 'info'">
                {{ row.isOnline ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="加入时间" width="160">
            <template #default="{ row }">
              {{ formatDate(row.joinTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <template v-if="row.role < 3">
                <el-button 
                  type="primary" 
                  text 
                  size="small"
                  @click="toggleAdmin(row)"
                >
                  {{ row.role === 2 ? '取消管理员' : '设为管理员' }}
                </el-button>
                <el-button 
                  type="warning" 
                  text 
                  size="small"
                  @click="toggleMute(row)"
                >
                  {{ row.status === 2 ? '解除禁言' : '禁言' }}
                </el-button>
                <el-button 
                  type="danger" 
                  text 
                  size="small"
                  @click="removeMember(row)"
                >
                  移除
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 危险操作 -->
      <el-card class="danger-card">
        <template #header>
          <div class="card-header danger">
            <span>危险操作</span>
          </div>
        </template>
        <el-button type="danger" @click="handleDelete">删除聊天室</el-button>
        <p class="warning-text">删除后，所有聊天记录将被清空，此操作不可恢复。</p>
      </el-card>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑聊天室" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="聊天室名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  getChatRoomDetail,
  getCourseStudents,
  getChatRoomMembers,
  updateChatRoom,
  updateChatRoomAnnouncement,
  deleteChatRoom,
  inviteChatMembers,
  inviteAllCourseStudents,
  setChatAdmin,
  muteChatMember,
  removeChatMember,
  type ChatRoom,
  type ChatMember,
  type CourseStudent
} from '@/api/chat'

const route = useRoute()
const router = useRouter()
// 保持字符串形式，避免大数精度丢失
const roomId = route.params.id as string

const loading = ref(true)
const loadingStudents = ref(false)
const room = ref<ChatRoom | null>(null)
const students = ref<CourseStudent[]>([])
const members = ref<ChatMember[]>([])
const selectedStudents = ref<number[]>([])
const announcement = ref('')
const inviting = ref(false)
const invitingAll = ref(false)
const showEditDialog = ref(false)

const editForm = ref({
  name: '',
  description: '',
  courseId: 0
})

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const goBack = () => {
  router.push('/chat')
}

const loadRoom = async () => {
  try {
    const res = await getChatRoomDetail(roomId)
    if (res.code == 200) {
      room.value = res.data
      announcement.value = res.data?.announcement || ''
      editForm.value = {
        name: res.data?.name || '',
        description: res.data?.description || '',
        courseId: res.data?.courseId || 0
      }
    }
  } catch (error) {
    console.error('加载聊天室失败:', error)
  }
}

const loadStudents = async () => {
  loadingStudents.value = true
  try {
    const res = await getCourseStudents(roomId)
    if (res.code == 200) {
      students.value = res.data || []
    }
  } catch (error) {
    console.error('加载学员失败:', error)
  } finally {
    loadingStudents.value = false
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

const saveAnnouncement = async () => {
  try {
    await updateChatRoomAnnouncement(roomId, announcement.value)
    ElMessage.success('公告已更新')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const saveEdit = async () => {
  try {
    await updateChatRoom(roomId, editForm.value)
    ElMessage.success('保存成功')
    showEditDialog.value = false
    loadRoom()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const inviteSelected = async () => {
  if (selectedStudents.value.length === 0) return
  
  inviting.value = true
  try {
    await inviteChatMembers({
      roomId,
      inviteeIds: selectedStudents.value,
      message: '讲师邀请您加入课程聊天室'
    })
    ElMessage.success('邀请已发送')
    selectedStudents.value = []
    loadStudents()
  } catch (error) {
    ElMessage.error('邀请失败')
  } finally {
    inviting.value = false
  }
}

const inviteAll = async () => {
  try {
    await ElMessageBox.confirm('确定要邀请该课程的所有学员吗？', '提示')
    invitingAll.value = true
    await inviteAllCourseStudents(roomId)
    ElMessage.success('邀请已发送给所有学员')
    loadStudents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('邀请失败')
    }
  } finally {
    invitingAll.value = false
  }
}

const toggleAdmin = async (member: ChatMember) => {
  try {
    const isAdmin = member.role !== 2
    await setChatAdmin(roomId, member.userId, isAdmin)
    ElMessage.success(isAdmin ? '已设为管理员' : '已取消管理员')
    loadMembers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const toggleMute = async (member: ChatMember) => {
  try {
    const mute = member.status !== 2
    await muteChatMember(roomId, member.userId, mute)
    ElMessage.success(mute ? '已禁言' : '已解除禁言')
    loadMembers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const removeMember = async (member: ChatMember) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员 ${member.nickname} 吗？`, '提示', {
      type: 'warning'
    })
    await removeChatMember(roomId, member.userId)
    ElMessage.success('已移除')
    loadMembers()
    loadStudents()
    if (room.value) {
      room.value.memberCount--
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该聊天室吗？删除后所有聊天记录将被清空，此操作不可恢复。',
      '危险操作',
      { type: 'error', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteChatRoom(roomId)
    ElMessage.success('已删除')
    router.push('/chat')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadRoom(), loadStudents(), loadMembers()])
  loading.value = false
})
</script>

<style lang="scss" scoped>
.chat-manage {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  
  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.manage-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  
  &.danger {
    color: var(--el-color-danger);
  }
}

.room-info {
  .info-row {
    display: flex;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      width: 100px;
      color: #666;
      flex-shrink: 0;
    }
    
    .value {
      color: #333;
    }
  }
}

.student-list {
  .student-item {
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .student-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .student-detail {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .name {
      font-weight: 500;
    }
  }
}

.member-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .name {
    font-weight: 500;
  }
}

.danger-card {
  .warning-text {
    margin-top: 12px;
    font-size: 13px;
    color: #999;
  }
}
</style>

