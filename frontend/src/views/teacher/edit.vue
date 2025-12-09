<template>
  <div class="teacher-edit-page">
    <div class="container" v-loading="loading">
      <div class="header">
        <h2>{{ isEdit ? '编辑课程' : '发布课程' }}</h2>
        <div class="actions">
          <el-button @click="router.back()">返回</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">{{ isEdit ? '保存更新' : '立即创建' }}</el-button>
        </div>
      </div>

      <el-form :model="form" label-width="90px" :rules="rules" ref="formRef">
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入课程标题" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" placeholder="可选" />
        </el-form-item>
        <el-form-item label="封面">
          <div class="cover-row">
            <el-input v-model="form.cover" placeholder="图片URL或上传" />
            <el-upload
              :show-file-list="false"
              :http-request="handleCoverUpload"
              accept="image/*"
            >
              <el-button type="primary" plain>上传封面</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">录播</el-radio>
            <el-radio :value="3">图文</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格">
          <div class="price-row">
            <el-input-number v-model="form.price" :precision="2" :step="10" :min="0" />
            <el-switch v-model="form.isFree" active-text="免费" inactive-text="收费" :active-value="1" :inactive-value="0" />
          </div>
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :precision="2" :step="10" :min="0" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简要介绍课程" />
        </el-form-item>
        <el-form-item v-if="form.type === 3" label="详情">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="课程详情，可使用富文本内容" />
        </el-form-item>
      </el-form>

      <div v-if="isEdit && form.type === 1" class="chapter-section">
        <div class="chapter-header">
          <h3>章节 / 课时</h3>
          <el-button type="primary" size="small" @click="openChapterDialog()">新增章节</el-button>
        </div>
        <el-empty v-if="!chapters.length" description="暂无章节" />
        <el-collapse v-else v-model="activeChapters">
          <el-collapse-item v-for="ch in chapters" :key="ch.id" :name="ch.id">
            <template #title>
              <div class="ch-title">
                <span>{{ ch.title }}</span>
                <div class="ch-actions">
                  <el-button text size="small" @click.stop="openChapterDialog(ch)">编辑</el-button>
                  <el-button text size="small" type="danger" @click.stop="removeChapter(ch)">删除</el-button>
                  <el-button text size="small" @click.stop="openLessonDialog(ch)">新增课时</el-button>
                  <el-upload
                    :show-file-list="false"
                    :http-request="(opt: UploadRequestOptions) => handleQuickVideoUpload(ch, opt)"
                    accept="video/*"
                  >
                    <el-button text size="small">上传视频并新建课时</el-button>
                  </el-upload>
                </div>
              </div>
            </template>
            <div class="lesson-list">
              <div v-for="ls in ch.lessons" :key="ls.id" class="lesson-item">
                <div class="ls-main">
                  <div class="ls-title">{{ ls.title }}</div>
                  <div class="ls-meta">
                    <span>时长：{{ formatDuration(ls.duration) }}</span>
                    <span>试看：{{ ls.isFree ? '是' : '否' }}</span>
                  </div>
                </div>
                <div class="ls-actions">
                  <el-button text size="small" @click="openLessonDialog(ch, ls)">编辑</el-button>
                  <el-button text size="small" type="danger" @click="removeLesson(ls)">删除</el-button>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 章节弹窗 -->
      <el-dialog v-model="chapterDialog.visible" :title="chapterDialog.isEdit ? '编辑章节' : '新增章节'" width="420px">
        <el-form label-width="70px">
          <el-form-item label="标题">
            <el-input v-model="chapterDialog.data.title" placeholder="请输入章节标题" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="chapterDialog.data.sort" :min="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="chapterDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveChapter">保存</el-button>
        </template>
      </el-dialog>

      <!-- 课时弹窗 -->
      <el-dialog v-model="lessonDialog.visible" :title="lessonDialog.isEdit ? '编辑课时' : '新增课时'" width="520px">
        <el-form label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="lessonDialog.data.title" placeholder="请输入课时标题" />
          </el-form-item>
          <el-form-item label="视频">
            <el-upload
              :show-file-list="false"
              :http-request="handleVideoUpload"
              accept="video/*"
            >
              <el-button type="primary" plain>上传视频</el-button>
              <span class="upload-tip" v-if="lessonDialog.data.videoUrl">已上传</span>
            </el-upload>
            <div v-if="lessonDialog.data.videoUrl" class="video-url">{{ lessonDialog.data.videoUrl }}</div>
          </el-form-item>
          <el-form-item label="时长(秒)">
            <el-input-number v-model="lessonDialog.data.duration" :min="0" />
          </el-form-item>
          <el-form-item label="试看">
            <el-switch v-model="lessonDialog.data.isFree" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="lessonDialog.data.sort" :min="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="lessonDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveLesson">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules, ElMessageBox, UploadRequestOptions } from 'element-plus'
import { getCategoryTree } from '@/api/course'
import { createCourse, updateCourse, createChapter, updateChapter, deleteChapter, createLesson, updateLesson, deleteLesson } from '@/api/teacher'
import type { CategoryTree, ChapterInfo, LessonInfo } from '@/types/course'
import { uploadVideo, uploadCourseCover } from '@/api/file'

const router = useRouter()
const route = useRoute()
const courseId = computed(() => route.params.id as string | undefined)
const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const categories = ref<CategoryTree[]>([])
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  subtitle: '',
  cover: '',
  description: '',
  content: '',
  categoryId: undefined as number | undefined,
  type: 1,
  price: 0,
  originalPrice: 0,
  isFree: 0
})
const chapters = ref<ChapterInfo[]>([])
const activeChapters = ref<number[]>([])

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const chapterDialog = reactive({
  visible: false,
  isEdit: false,
  data: { id: undefined as number | undefined, title: '', sort: 0 }
})

const lessonDialog = reactive({
  visible: false,
  isEdit: false,
  chapterId: undefined as number | undefined,
  data: { id: undefined as number | undefined, title: '', videoUrl: '', duration: 0, isFree: 0, sort: 0 }
})

async function loadCategories() {
  const res = await getCategoryTree()
  categories.value = (res.data || [])
    .map((c: CategoryTree) => ({ ...c, level: Number(c.level) }))
    .filter((c: CategoryTree) => c.level === 1)
}

async function loadDetail() {
  if (!courseId.value) return
  loading.value = true
  try {
    // 复用课程详情接口
    const { getCourseDetail } = await import('@/api/course')
    const res = await getCourseDetail(courseId.value)
    Object.assign(form, {
      title: res.data.title,
      subtitle: res.data.subtitle,
      cover: res.data.cover,
      description: res.data.description,
      content: res.data.content,
      categoryId: res.data.categoryId,
      type: res.data.type,
      price: Number(res.data.price),
      originalPrice: Number(res.data.originalPrice),
      isFree: Number(res.data.isFree)
    })
    chapters.value = res.data.chapters || []
    if (chapters.value.length) activeChapters.value = [chapters.value[0].id]
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (isEdit.value) {
        await updateCourse(courseId.value as any, form)
        ElMessage.success('更新成功')
      } else {
        const res = await createCourse(form)
        ElMessage.success('创建成功')
        router.replace(`/teacher/edit/${res.data}`)
      }
    } catch (e) {
      console.error(e)
    } finally {
      saving.value = false
    }
  })
}

function formatDuration(seconds?: number) {
  if (!seconds) return '00:00'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

function openChapterDialog(ch?: ChapterInfo) {
  chapterDialog.visible = true
  chapterDialog.isEdit = !!ch
  chapterDialog.data = {
    id: ch?.id,
    title: ch?.title || '',
    sort: ch?.sort || 0,
    courseId: courseId.value
  } as any
}

async function saveChapter() {
  const payload: any = {
    courseId: courseId.value,
    title: chapterDialog.data.title,
    sort: chapterDialog.data.sort
  }
  if (chapterDialog.isEdit && chapterDialog.data.id) {
    await updateChapter(chapterDialog.data.id, payload)
    ElMessage.success('章节已更新')
  } else {
    await createChapter(payload)
    ElMessage.success('章节已创建')
  }
  chapterDialog.visible = false
  await loadDetail()
}

async function removeChapter(ch: ChapterInfo) {
  ElMessageBox.confirm('删除该章节会同时删除其下课时，确认删除？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteChapter(ch.id)
      ElMessage.success('已删除章节')
      await loadDetail()
    })
    .catch(() => {})
}

function openLessonDialog(ch: ChapterInfo, ls?: LessonInfo) {
  lessonDialog.visible = true
  lessonDialog.isEdit = !!ls
  lessonDialog.chapterId = ch.id
  lessonDialog.data = {
    id: ls?.id,
    title: ls?.title || '',
    videoUrl: ls?.videoUrl || '',
    duration: ls?.duration || 0,
    isFree: ls?.isFree || 0,
    sort: ls?.sort || 0
  }
}

async function handleVideoUpload(options: UploadRequestOptions) {
  try {
    const url = await uploadVideo(options.file as File)
    lessonDialog.data.videoUrl = url
    ElMessage.success('视频上传成功')
    options.onSuccess && options.onSuccess(url as any)
  } catch (e: any) {
    options.onError && options.onError(e)
  }
}

async function handleCoverUpload(options: UploadRequestOptions) {
  try {
    const url = await uploadCourseCover(options.file as File)
    form.cover = url
    ElMessage.success('封面上传成功')
    options.onSuccess && options.onSuccess(url as any)
  } catch (e: any) {
    options.onError && options.onError(e)
  }
}

async function handleQuickVideoUpload(ch: ChapterInfo, options: UploadRequestOptions) {
  try {
    const url = await uploadVideo(options.file as File)
    // 以文件名作为课时标题（去掉扩展名）
    const name = (options.file as File).name || '新课时'
    const title = name.replace(/\.[^.]+$/, '')
    await createLesson({
      courseId: courseId.value,
      chapterId: ch.id,
      title,
      videoUrl: url,
      duration: 0,
      isFree: 0,
      sort: (ch.lessons?.length || 0) + 1,
      type: 1
    })
    ElMessage.success('视频上传并创建课时成功')
    options.onSuccess && options.onSuccess(url as any)
    await loadDetail()
  } catch (e: any) {
    options.onError && options.onError(e)
  }
}

async function saveLesson() {
  if (!lessonDialog.chapterId) return
  const payload: any = {
    courseId: courseId.value,
    chapterId: lessonDialog.chapterId,
    title: lessonDialog.data.title,
    videoUrl: lessonDialog.data.videoUrl,
    duration: lessonDialog.data.duration,
    isFree: lessonDialog.data.isFree,
    sort: lessonDialog.data.sort,
    type: 1
  }
  if (lessonDialog.isEdit && lessonDialog.data.id) {
    await updateLesson(lessonDialog.data.id, payload)
    ElMessage.success('课时已更新')
  } else {
    await createLesson(payload)
    ElMessage.success('课时已创建')
  }
  lessonDialog.visible = false
  await loadDetail()
}

async function removeLesson(ls: LessonInfo) {
  ElMessageBox.confirm('确认删除该课时？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteLesson(ls.id)
      ElMessage.success('已删除课时')
      await loadDetail()
    })
    .catch(() => {})
}

onMounted(() => {
  loadCategories()
  loadDetail()
})
</script>

<style scoped lang="scss">
.teacher-edit-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 900px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h2 { font-size: 18px; }
  .actions { display: flex; gap: 10px; }
}
.price-row { display: flex; align-items: center; gap: 14px; }
.chapter-section { margin-top: 20px; border-top: 1px solid #f0f0f0; padding-top: 12px;
  .chapter-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
  .ch-title { display: flex; justify-content: space-between; width: 100%; align-items: center; gap: 8px; }
  .ch-actions { display: flex; gap: 8px; }
  .lesson-list { padding: 10px 0;
    .lesson-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #f7f7f7;
      &:last-child { border-bottom: none; }
      .ls-title { font-weight: 600; }
      .ls-meta { font-size: 12px; color: var(--text-muted); display: flex; gap: 12px; }
      .ls-actions { display: flex; gap: 8px; }
    }
  }
}
.upload-tip { margin-left: 8px; font-size: 12px; color: var(--text-muted); }
.video-url { margin-top: 6px; font-size: 12px; color: var(--primary-color); word-break: break-all; }
</style>

