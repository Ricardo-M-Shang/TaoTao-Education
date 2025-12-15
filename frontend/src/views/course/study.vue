<template>
  <div class="study-page" v-loading="loading">
    <template v-if="course">
      <!-- 顶部导航 -->
      <div class="study-header">
        <div class="back" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </div>
        <h1>{{ course.title }}</h1>
        <div class="progress-info">
          <span>学习进度：{{ courseProgress }}%</span>
          <el-progress :percentage="courseProgress" :show-text="false" :stroke-width="4" />
        </div>
      </div>

      <div class="study-body">
        <!-- 视频区域 -->
        <div class="video-section">
          <div class="video-container">
            <video
              ref="videoRef"
              :src="currentLesson?.videoUrl || ''"
              controls
              @timeupdate="onTimeUpdate"
              @ended="onVideoEnded"
              @play="onVideoPlay"
            >
              您的浏览器不支持视频播放
            </video>
            <div class="video-overlay" v-if="!currentLesson?.videoUrl">
              <el-icon :size="48"><VideoPlay /></el-icon>
              <p>暂无视频资源</p>
            </div>
          </div>
          <div class="lesson-info">
            <div class="lesson-main">
              <h2>{{ currentLesson?.title || '请选择课时' }}</h2>
              <div class="lesson-meta" v-if="currentLesson">
                <span>时长：{{ formatDuration(currentLesson.duration) }}</span>
                <span v-if="lessonRecord">已学习：{{ formatDuration(lessonRecord.duration || 0) }}</span>
                <el-tag v-if="lessonRecord?.isFinished" type="success" size="small">已完成</el-tag>
              </div>
            </div>
            <div class="lesson-actions">
              <el-button size="small" :type="showNotes ? 'primary' : 'default'" @click="toggleNotes">
                <el-icon><EditPen /></el-icon>
                笔记
              </el-button>
              <el-button size="small" @click="handleAddNote">
                <el-icon><Plus /></el-icon>
                添加笔记
              </el-button>
            </div>
          </div>
          
          <!-- 笔记区域 -->
          <transition name="slide-down">
            <div class="notes-panel" v-show="showNotes">
              <div class="notes-header">
                <h3>
                  <el-icon><Notebook /></el-icon>
                  课时笔记
                </h3>
                <span class="note-count">{{ lessonNotes.length }}条笔记</span>
              </div>
              <div class="notes-list" v-if="lessonNotes.length">
                <div v-for="note in lessonNotes" :key="note.id" class="note-item">
                  <div class="note-header">
                    <span class="note-time" v-if="note.videoTime" @click="seekToTime(note.videoTime)">
                      <el-icon><VideoPlay /></el-icon>
                      {{ formatDuration(note.videoTime) }}
                    </span>
                    <span class="note-date">{{ formatNoteTime(note.createTime) }}</span>
                    <div class="note-actions">
                      <el-button link size="small" @click="editNote(note)">编辑</el-button>
                      <el-button link size="small" type="danger" @click="handleDeleteNote(note)">删除</el-button>
                    </div>
                  </div>
                  <div class="note-content">{{ note.content }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无笔记，点击上方按钮添加" :image-size="60" />
            </div>
          </transition>
        </div>

        <!-- 侧边栏 - 课程目录 -->
        <div class="sidebar">
          <div class="sidebar-header">
            <h3>课程目录</h3>
            <span class="count">共{{ totalLessons }}课时</span>
          </div>
          <div class="chapter-list">
            <div v-for="chapter in course.chapters" :key="chapter.id" class="chapter">
              <div class="chapter-title" @click="toggleChapter(chapter.id)">
                <span>{{ chapter.title }}</span>
                <el-icon :class="{ expanded: expandedChapters.includes(chapter.id) }"><ArrowRight /></el-icon>
              </div>
              <transition name="slide">
                <div class="lesson-list" v-show="expandedChapters.includes(chapter.id)">
                  <div
                    v-for="lesson in chapter.lessons"
                    :key="lesson.id"
                    :class="['lesson-item', { active: currentLesson?.id === lesson.id, finished: isLessonFinished(lesson.id) }]"
                    @click="selectLesson(lesson, chapter.id)"
                  >
                    <div class="lesson-status">
                      <el-icon v-if="isLessonFinished(lesson.id)" class="icon-finished"><CircleCheck /></el-icon>
                      <el-icon v-else-if="currentLesson?.id === lesson.id" class="icon-playing"><VideoPlay /></el-icon>
                      <span v-else class="lesson-num">{{ getLessonIndex(chapter.id, lesson.id) }}</span>
                    </div>
                    <div class="lesson-content">
                      <span class="lesson-title">{{ lesson.title }}</span>
                      <span class="lesson-duration">{{ formatDuration(lesson.duration) }}</span>
                    </div>
                    <div class="lesson-progress" v-if="getLessonProgress(lesson.id) > 0 && !isLessonFinished(lesson.id)">
                      <el-progress :percentage="getLessonProgress(lesson.id)" :show-text="false" :stroke-width="2" />
                    </div>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 添加/编辑笔记弹窗 -->
    <el-dialog v-model="noteDialogVisible" :title="editingNote ? '编辑笔记' : '添加笔记'" width="500px">
      <el-form @submit.prevent>
        <el-form-item v-if="!editingNote">
          <el-checkbox v-model="noteForm.linkToTime">关联当前视频时间点</el-checkbox>
          <span v-if="noteForm.linkToTime" class="time-hint">
            {{ formatDuration(Math.floor(videoRef?.currentTime || 0)) }}
          </span>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="noteForm.content"
            type="textarea"
            :rows="6"
            placeholder="记录你的学习笔记..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveNote" :loading="savingNote">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, VideoPlay, CircleCheck, EditPen, Plus, Notebook } from '@element-plus/icons-vue'
import { getCourseDetail } from '@/api/course'
import { getCourseStudyRecords, updateStudyProgress, getCourseProgress, StudyRecord } from '@/api/study'
import { updateProgress as updateUserCourseProgress } from '@/api/userCourse'
import { getLessonNotes, saveNote, updateNote, deleteNote, NoteInfo } from '@/api/note'
import type { CourseDetail, LessonInfo } from '@/types/course'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const course = ref<CourseDetail | null>(null)
const currentLesson = ref<LessonInfo | null>(null)
const currentChapterId = ref<number | null>(null)
const expandedChapters = ref<number[]>([])
const studyRecords = ref<StudyRecord[]>([])
const courseProgress = ref(0)
const videoRef = ref<HTMLVideoElement | null>(null)

// 笔记相关
const showNotes = ref(false)
const lessonNotes = ref<NoteInfo[]>([])
const noteDialogVisible = ref(false)
const editingNote = ref<NoteInfo | null>(null)
const savingNote = ref(false)
const noteForm = reactive({
  content: '',
  linkToTime: true
})

// 当前课时学习记录
const lessonRecord = computed(() => {
  if (!currentLesson.value) return null
  return studyRecords.value.find(r => r.lessonId === currentLesson.value!.id)
})

// 总课时数
const totalLessons = computed(() => {
  if (!course.value) return 0
  return course.value.chapters?.reduce((sum, ch) => sum + (ch.lessons?.length || 0), 0) || 0
})

// 进度上报定时器
let progressTimer: ReturnType<typeof setInterval> | null = null
let lastReportTime = 0

function formatDuration(seconds: number) {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

function formatNoteTime(time: string): string {
  if (!time) return ''
  return time.substring(0, 16).replace('T', ' ')
}

function toggleChapter(chapterId: number) {
  const index = expandedChapters.value.indexOf(chapterId)
  if (index > -1) {
    expandedChapters.value.splice(index, 1)
  } else {
    expandedChapters.value.push(chapterId)
  }
}

function isLessonFinished(lessonId: number) {
  const record = studyRecords.value.find(r => r.lessonId === lessonId)
  return record?.isFinished === 1
}

function getLessonProgress(lessonId: number) {
  const record = studyRecords.value.find(r => r.lessonId === lessonId)
  return record?.progress || 0
}

function getLessonIndex(chapterId: number, lessonId: number): number {
  if (!course.value) return 0
  let index = 0
  for (const ch of course.value.chapters || []) {
    for (const ls of ch.lessons || []) {
      index++
      if (ch.id === chapterId && ls.id === lessonId) return index
    }
  }
  return index
}

function selectLesson(lesson: LessonInfo, chapterId: number) {
  // 保存当前课时进度
  if (currentLesson.value) {
    saveProgress()
  }
  
  currentLesson.value = lesson
  currentChapterId.value = chapterId
  
  // 重置视频
  if (videoRef.value) {
    videoRef.value.currentTime = 0
    
    // 如果有学习记录，跳转到上次位置
    const record = studyRecords.value.find(r => r.lessonId === lesson.id)
    if (record && record.progress < 90 && lesson.duration) {
      const seekTime = (record.progress / 100) * lesson.duration
      videoRef.value.currentTime = seekTime
    }
  }
  
  // 加载笔记
  loadLessonNotes()
}

// 笔记功能
function toggleNotes() {
  showNotes.value = !showNotes.value
}

async function loadLessonNotes() {
  if (!currentLesson.value) return
  try {
    const res = await getLessonNotes(currentLesson.value.id)
    lessonNotes.value = res.data || []
  } catch (e) {
    console.error('加载笔记失败', e)
  }
}

function handleAddNote() {
  editingNote.value = null
  noteForm.content = ''
  noteForm.linkToTime = true
  noteDialogVisible.value = true
}

function editNote(note: NoteInfo) {
  editingNote.value = note
  noteForm.content = note.content
  noteDialogVisible.value = true
}

function seekToTime(time: number) {
  if (videoRef.value) {
    videoRef.value.currentTime = time
    videoRef.value.play()
  }
}

async function handleSaveNote() {
  if (!noteForm.content.trim()) {
    ElMessage.warning('请输入笔记内容')
    return
  }
  
  if (!currentLesson.value || !course.value) return
  
  savingNote.value = true
  try {
    if (editingNote.value) {
      await updateNote(editingNote.value.id, noteForm.content)
      ElMessage.success('笔记已更新')
    } else {
      await saveNote({
        courseId: course.value.id,
        lessonId: currentLesson.value.id,
        content: noteForm.content,
        videoTime: noteForm.linkToTime ? Math.floor(videoRef.value?.currentTime || 0) : undefined
      })
      ElMessage.success('笔记已保存')
    }
    noteDialogVisible.value = false
    loadLessonNotes()
  } catch (e) {
    console.error('保存笔记失败', e)
  } finally {
    savingNote.value = false
  }
}

async function handleDeleteNote(note: NoteInfo) {
  ElMessageBox.confirm('确定删除这条笔记吗？', '删除确认', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNote(note.id)
      ElMessage.success('笔记已删除')
      loadLessonNotes()
    } catch (e) {
      console.error('删除笔记失败', e)
    }
  }).catch(() => {})
}

function onVideoPlay() {
  startProgressReport()
}

function onTimeUpdate() {
  // 由定时器处理进度上报
}

function onVideoEnded() {
  saveProgress(true)
  playNextLesson()
}

function startProgressReport() {
  if (progressTimer) return
  progressTimer = setInterval(() => {
    const now = Date.now()
    if (now - lastReportTime >= 10000) {
      saveProgress()
      lastReportTime = now
    }
  }, 1000)
}

function stopProgressReport() {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

async function saveProgress(isFinished = false) {
  if (!currentLesson.value || !course.value || !videoRef.value) return
  
  const duration = Math.floor(videoRef.value.currentTime)
  let progress = 0
  if (currentLesson.value.duration && currentLesson.value.duration > 0) {
    progress = Math.min(100, Math.floor((videoRef.value.currentTime / currentLesson.value.duration) * 100))
  }
  
  if (isFinished) {
    progress = 100
  }
  
  try {
    await updateStudyProgress({
      courseId: course.value.id,
      lessonId: currentLesson.value.id,
      chapterId: currentChapterId.value || undefined,
      duration: 10,
      progress
    })
    
    // 更新本地记录
    const existingIndex = studyRecords.value.findIndex(r => r.lessonId === currentLesson.value!.id)
    if (existingIndex > -1) {
      studyRecords.value[existingIndex].progress = progress
      studyRecords.value[existingIndex].duration = duration
      if (progress >= 90) {
        studyRecords.value[existingIndex].isFinished = 1
      }
    } else {
      studyRecords.value.push({
        id: 0,
        courseId: course.value.id,
        lessonId: currentLesson.value.id,
        chapterId: currentChapterId.value || 0,
        duration,
        progress,
        isFinished: progress >= 90 ? 1 : 0,
        updateTime: new Date().toISOString()
      })
    }
    
    await refreshCourseProgress()
  } catch (e) {
    console.error('保存进度失败', e)
  }
}

async function refreshCourseProgress() {
  if (!course.value) return
  try {
    const res = await getCourseProgress(course.value.id)
    courseProgress.value = res.data || 0
    await updateUserCourseProgress(course.value.id, courseProgress.value)
  } catch (e) {
    console.error(e)
  }
}

function playNextLesson() {
  if (!course.value || !currentLesson.value) return
  
  let foundCurrent = false
  for (const chapter of course.value.chapters || []) {
    for (const lesson of chapter.lessons || []) {
      if (foundCurrent) {
        selectLesson(lesson, chapter.id)
        if (!expandedChapters.value.includes(chapter.id)) {
          expandedChapters.value.push(chapter.id)
        }
        ElMessage.success('已自动切换到下一课时')
        return
      }
      if (lesson.id === currentLesson.value.id) {
        foundCurrent = true
      }
    }
  }
  
  ElMessage.success('恭喜！您已完成所有课时学习')
}

async function load() {
  const id = route.params.id as string
  if (!id) return
  loading.value = true
  try {
    const [courseRes, recordsRes, progressRes] = await Promise.all([
      getCourseDetail(id),
      getCourseStudyRecords(id as any),
      getCourseProgress(id as any)
    ])
    
    course.value = courseRes.data
    studyRecords.value = recordsRes.data || []
    courseProgress.value = progressRes.data || 0
    
    if (course.value.chapters?.length) {
      expandedChapters.value = [course.value.chapters[0].id]
    }
    
    const lessonId = route.query.lesson ? Number(route.query.lesson) : null
    if (lessonId && course.value.chapters) {
      for (const ch of course.value.chapters) {
        const lesson = ch.lessons?.find(l => l.id === lessonId)
        if (lesson) {
          selectLesson(lesson, ch.id)
          if (!expandedChapters.value.includes(ch.id)) {
            expandedChapters.value.push(ch.id)
          }
          break
        }
      }
    } else if (course.value.chapters?.length && course.value.chapters[0].lessons?.length) {
      selectLesson(course.value.chapters[0].lessons[0], course.value.chapters[0].id)
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(load)

onUnmounted(() => {
  stopProgressReport()
  if (currentLesson.value) {
    saveProgress()
  }
})

watch(() => route.query.lesson, (newVal) => {
  if (newVal && course.value) {
    const lessonId = Number(newVal)
    for (const ch of course.value.chapters || []) {
      const lesson = ch.lessons?.find(l => l.id === lessonId)
      if (lesson) {
        selectLesson(lesson, ch.id)
        break
      }
    }
  }
})
</script>

<style lang="scss" scoped>
.study-page {
  min-height: 100vh;
  background: #0f0f0f;
  display: flex;
  flex-direction: column;
}

.study-header {
  height: 56px;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 20px;
  border-bottom: 1px solid #2a2a2a;
  
  .back {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #999;
    font-size: 13px;
    cursor: pointer;
    transition: color 0.2s;
    &:hover { color: #fff; }
  }
  
  h1 {
    flex: 1;
    font-size: 15px;
    font-weight: 500;
    color: #fff;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .progress-info {
    display: flex;
    align-items: center;
    gap: 10px;
    span { font-size: 12px; color: #999; }
    :deep(.el-progress) { width: 100px; }
  }
}

.study-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.video-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  
  .video-container {
    flex: 1;
    background: #000;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    
    video {
      width: 100%;
      max-height: 100%;
    }
    
    .video-overlay {
      position: absolute;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      color: #666;
      p { font-size: 13px; }
    }
  }
  
  .lesson-info {
    padding: 16px 20px;
    background: #1a1a1a;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .lesson-main {
      h2 {
        font-size: 16px;
        font-weight: 500;
        color: #fff;
        margin-bottom: 8px;
      }
      
      .lesson-meta {
        display: flex;
        align-items: center;
        gap: 16px;
        font-size: 12px;
        color: #999;
      }
    }
    
    .lesson-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .notes-panel {
    background: #1e1e1e;
    border-top: 1px solid #2a2a2a;
    max-height: 250px;
    overflow-y: auto;
    
    .notes-header {
      padding: 12px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #2a2a2a;
      position: sticky;
      top: 0;
      background: #1e1e1e;
      
      h3 {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #fff;
        
        .el-icon { color: #6366f1; }
      }
      
      .note-count {
        font-size: 11px;
        color: #666;
      }
    }
    
    .notes-list {
      padding: 12px 20px;
    }
    
    .note-item {
      padding: 12px;
      background: #252525;
      border-radius: 8px;
      margin-bottom: 10px;
      
      &:last-child { margin-bottom: 0; }
      
      .note-header {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;
        
        .note-time {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 11px;
          color: #6366f1;
          cursor: pointer;
          padding: 2px 8px;
          background: rgba(99, 102, 241, 0.1);
          border-radius: 4px;
          
          &:hover { background: rgba(99, 102, 241, 0.2); }
        }
        
        .note-date {
          flex: 1;
          font-size: 11px;
          color: #666;
        }
        
        .note-actions {
          opacity: 0;
          transition: opacity 0.2s;
        }
      }
      
      &:hover .note-actions { opacity: 1; }
      
      .note-content {
        font-size: 12px;
        color: #ccc;
        line-height: 1.6;
        white-space: pre-wrap;
      }
    }
    
    :deep(.el-empty) {
      padding: 20px;
      
      .el-empty__description { color: #666; }
    }
  }
}

.sidebar {
  width: 320px;
  background: #1a1a1a;
  border-left: 1px solid #2a2a2a;
  display: flex;
  flex-direction: column;
  
  .sidebar-header {
    padding: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #2a2a2a;
    
    h3 { font-size: 14px; color: #fff; }
    .count { font-size: 12px; color: #666; }
  }
  
  .chapter-list {
    flex: 1;
    overflow-y: auto;
    
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: #333; border-radius: 2px; }
  }
  
  .chapter {
    .chapter-title {
      padding: 12px 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      color: #ddd;
      background: #222;
      cursor: pointer;
      transition: background 0.2s;
      
      &:hover { background: #2a2a2a; }
      
      .el-icon {
        transition: transform 0.3s;
        &.expanded { transform: rotate(90deg); }
      }
    }
    
    .lesson-list {
      background: #1a1a1a;
    }
  }
  
  .lesson-item {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    gap: 10px;
    cursor: pointer;
    transition: background 0.2s;
    border-bottom: 1px solid #222;
    
    &:hover { background: #252525; }
    &.active { background: #2a2a4a; }
    &.finished {
      .lesson-title { color: #888; }
    }
    
    .lesson-status {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .lesson-num {
        font-size: 11px;
        color: #666;
      }
      
      .icon-finished { color: #10b981; font-size: 18px; }
      .icon-playing { color: var(--primary-color); font-size: 18px; }
    }
    
    .lesson-content {
      flex: 1;
      min-width: 0;
      
      .lesson-title {
        display: block;
        font-size: 12px;
        color: #ccc;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .lesson-duration {
        font-size: 10px;
        color: #666;
      }
    }
    
    .lesson-progress {
      width: 40px;
      :deep(.el-progress-bar__outer) { background: #333; }
    }
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}
.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}
.slide-enter-to,
.slide-leave-from {
  max-height: 500px;
  opacity: 1;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  max-height: 0;
  opacity: 0;
}
.slide-down-enter-to,
.slide-down-leave-from {
  max-height: 250px;
  opacity: 1;
}

.time-hint {
  margin-left: 8px;
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}
</style>
