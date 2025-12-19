<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="course">
      <!-- 头部 -->
      <div class="header">
        <div class="header-inner">
          <div class="cover"><img :src="course.cover || defaultCover" /></div>
          <div class="info">
            <h1>{{ course.title }}</h1>
            <p class="sub">{{ course.subtitle }}</p>
            <div class="meta">
              <span>{{ course.teacherName }}</span>
              <span>{{ course.lessonCount }}课时</span>
              <span>{{ course.studyCount }}人学习</span>
              <span><el-rate v-model="course.score" disabled show-score text-color="#fff" score-template="{value}分" /></span>
            </div>
            <div class="action">
              <div class="price" v-if="course.isFree">免费</div>
              <div class="price" v-else>
                ¥{{ course.price }}
                <del v-if="course.originalPrice > course.price">¥{{ course.originalPrice }}</del>
                <span v-if="selectedCouponId" class="final-pay">预计支付 ¥{{ payAmount }}</span>
              </div>
              <div class="coupon-row" v-if="!course.isFree">
                <el-select
                  v-model="selectedCouponId"
                  clearable
                  :placeholder="availableCoupons.length ? `有 ${availableCoupons.length} 张优惠券可用` : '暂无可用优惠券'"
                  :loading="loadingCoupons"
                  size="default"
                  class="coupon-select"
                  @change="onCouponChange"
                >
                  <el-option
                    v-for="c in availableCoupons"
                    :key="c.userCouponId || c.id"
                    :label="couponLabel(c)"
                    :value="c.userCouponId"
                  />
                </el-select>
              </div>
              <el-button type="primary" round :disabled="hasBought" @click="handleBuy">
                {{ hasBought ? '已购买' : (course.isFree ? '立即学习' : '立即购买') }}
              </el-button>
              <el-button v-if="hasBought" type="success" round @click="goStudy">开始学习</el-button>
              <el-button :type="isFavorite ? 'warning' : 'default'" round @click="toggleFavorite">
                <el-icon><Star /></el-icon>
                {{ isFavorite ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 内容 -->
      <div class="content">
        <div class="main">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="课程介绍" name="intro">
              <div class="intro">
                <h3>简介</h3>
                <p>{{ course.description }}</p>
                <div v-html="course.content"></div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="课程目录" name="chapters">
              <el-collapse v-model="activeChapters">
                <el-collapse-item v-for="ch in course.chapters" :key="ch.id" :name="ch.id">
                  <template #title><div class="ch-title"><span>{{ ch.title }}</span><span class="cnt">{{ ch.lessons?.length || 0 }}课时</span></div></template>
                  <div v-for="ls in ch.lessons" :key="ls.id" class="lesson" @click="handleLessonClick(ls)">
                    <span class="name">{{ ls.title }}</span>
                    <span class="dur">{{ Math.floor(ls.duration/60) }}:{{ (ls.duration%60).toString().padStart(2,'0') }}</span>
                    <el-tag v-if="ls.isFree" size="small" type="success">试看</el-tag>
                    <el-icon v-if="getLessonFinished(ls.id)" class="finished"><CircleCheck /></el-icon>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </el-tab-pane>
            <el-tab-pane label="学员评价" name="reviews">
              <div class="reviews-section">
                <!-- 评价统计 -->
                <div class="review-stats">
                  <div class="avg-score">
                    <span class="num">{{ course.score || '0.0' }}</span>
                    <el-rate v-model="course.score" disabled />
                    <span class="total">{{ reviewTotal }}条评价</span>
                  </div>
                </div>

                <!-- 发表评价 -->
                <div class="add-review" v-if="hasBought && !hasReviewed">
                  <h4>发表评价</h4>
                  <div class="review-form">
                    <div class="row">
                      <span class="label">评分：</span>
                      <el-rate v-model="reviewForm.score" />
                    </div>
                    <div class="row">
                      <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="写下您的学习感受..." maxlength="500" show-word-limit />
                    </div>
                    <div class="row">
                      <el-checkbox v-model="reviewForm.isAnonymous" :true-value="1" :false-value="0">匿名评价</el-checkbox>
                      <el-button type="primary" size="small" :loading="submittingReview" @click="submitReview">提交评价</el-button>
                    </div>
                  </div>
                </div>
                <div class="add-review tip" v-else-if="hasReviewed">
                  <el-alert title="您已评价过该课程" type="success" :closable="false" />
                </div>
                <div class="add-review tip" v-else-if="!hasBought">
                  <el-alert title="购买课程后才能评价哦~" type="info" :closable="false" />
                </div>

                <!-- 评价列表 -->
                <div class="review-list">
                  <div v-for="r in reviews" :key="r.id" class="review-item">
                    <el-avatar :size="36" :src="r.avatar || defaultAvatar" />
                    <div class="review-body">
                      <div class="review-header">
                        <span class="name">{{ r.nickname || '用户' }}</span>
                        <el-rate v-model="r.score" disabled size="small" />
                      </div>
                      <p class="review-content">{{ r.content || '用户未填写评价内容' }}</p>
                      <span class="review-time">{{ formatTime(r.createTime) }}</span>
                    </div>
                  </div>
                  <el-empty v-if="!reviews.length" description="暂无评价" />
                  <div class="load-more" v-if="reviewTotal > reviews.length">
                    <el-button text type="primary" @click="loadMoreReviews">加载更多</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          <el-tab-pane label="问答" name="qa">
            <div class="qa-section">
              <div class="ask-box" v-if="hasBought">
                <el-input
                  v-model="questionForm.content"
                  type="textarea"
                  :rows="3"
                  placeholder="提问与课程相关的问题"
                  maxlength="300"
                  show-word-limit
                />
                <div class="ask-actions">
                  <el-button type="primary" size="small" :loading="submittingQuestion" @click="submitQuestion">发布提问</el-button>
                </div>
              </div>
              <div class="ask-tip" v-else>
                <el-alert title="购买课程后可提问" type="info" :closable="false" />
              </div>

              <div class="qa-list">
                <div v-for="q in questions" :key="q.id" class="qa-item">
                  <div class="q-header">
                    <div class="q-user">{{ q.nickname || q.username || '用户' }}</div>
                    <div class="q-time">{{ formatTime(q.createTime) }}</div>
                  </div>
                  <div class="q-content">{{ q.content }}</div>
                  <div class="answer-box" v-if="hasBought">
                    <el-input
                      v-model="answerInputs[q.id as any]"
                      type="textarea"
                      :rows="2"
                      placeholder="写下你的回答"
                      maxlength="300"
                      show-word-limit
                    />
                    <div class="answer-actions">
                      <el-button size="small" :loading="submittingAnswerId === q.id" @click="submitAnswer(q)">回答</el-button>
                    </div>
                  </div>
                  <div class="answers" v-if="q.answers?.length">
                    <div v-for="a in q.answers" :key="a.id" class="answer-item">
                      <div class="ans-header">
                        <span class="ans-user">{{ a.nickname || a.username || '用户' }}</span>
                        <span class="ans-time">{{ formatTime(a.createTime) }}</span>
                        <el-tag v-if="a.accepted" size="small" type="success">已采纳</el-tag>
                        <el-button
                          v-else-if="userStore.userInfo?.id === q.userId"
                          text
                          size="small"
                          type="primary"
                          :loading="acceptingId === a.id"
                          @click="doAccept(a.id)"
                        >采纳</el-button>
                      </div>
                      <div class="ans-content">{{ a.content }}</div>
                    </div>
                  </div>
                </div>
                <el-empty v-if="!questions.length" description="暂无问答" />
                <div class="load-more" v-if="questionTotal > questions.length">
                  <el-button text type="primary" @click="loadMoreQuestionsFn">加载更多</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
          </el-tabs>
        </div>
        <div class="side">
          <div class="teacher-card">
            <h4>讲师</h4>
            <el-avatar :size="48" />
            <div class="name">{{ course.teacherName }}</div>
          </div>
          <div class="recommend-card" v-if="recommends.length">
            <h4>猜你喜欢</h4>
            <div class="rec-item" v-for="r in recommends" :key="r.id" @click="goRecommend(r.id)">
              <img :src="r.cover || defaultCover" />
              <div class="rec-info">
                <p class="title">{{ r.title }}</p>
                <p class="meta">{{ r.teacherName }} · {{ r.studyCount }}人学</p>
                <p class="price" v-if="r.isFree">免费</p>
                <p class="price" v-else>¥{{ r.price }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, CircleCheck } from '@element-plus/icons-vue'
import { getCourseDetail } from '@/api/course'
import { createOrder, checkBuyCourse, getAvailableCoupons } from '@/api/order'
import { addReview, getReviewList, checkReviewed, ReviewInfo } from '@/api/review'
import { addQuestion, addAnswer, getQuestionList, acceptAnswer, type QuestionItem } from '@/api/qa'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { getCourseStudyRecords, StudyRecord } from '@/api/study'
import { getCourseList } from '@/api/course'
import { useUserStore } from '@/stores/user'
import type { CourseDetail, LessonInfo } from '@/types/course'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const course = ref<CourseDetail | null>(null)
const activeTab = ref('intro')
const activeChapters = ref<number[]>([])
const hasBought = ref(false)
const isFavorite = ref(false)
const hasReviewed = ref(false)
const selectedCouponId = ref<number | string | null>(null)
const availableCoupons = ref<any[]>([])
const loadingCoupons = ref(false)
const payAmount = computed(() => {
  if (!course.value) return 0
  const price = Number(course.value.price || 0)
  if (!selectedCouponId.value) return price
  
  const c = availableCoupons.value.find((it) => String(it.userCouponId) === String(selectedCouponId.value))
  if (!c) return price
  
  // 检查使用门槛
  const thresholdAmount = Number(c.thresholdAmount || 0)
  if (thresholdAmount > 0 && price < thresholdAmount) {
    console.warn(`优惠券使用门槛不满足：需要¥${thresholdAmount}，当前¥${price}`)
    return price
  }
  
  let discount = 0
  const type = Number(c.type)
  if (type === 1) {
    // 满减券
    discount = Number(c.discountAmount || 0)
  } else if (type === 2) {
    // 折扣券
    const rate = Number(c.discountRate || 1)
    discount = price - Number((price * rate).toFixed(2))
  }
  
  // 确保折扣不超过商品价格
  if (discount < 0) discount = 0
  if (discount > price) discount = price
  
  const finalPrice = Number((price - discount).toFixed(2))
  console.log('价格计算:', { 原价: price, 优惠券: c.name, 门槛: thresholdAmount, 折扣: discount, 最终价格: finalPrice })
  
  return finalPrice
})
const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=600'
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 评价相关
const reviews = ref<ReviewInfo[]>([])
const reviewTotal = ref(0)
const reviewPage = ref(1)
const submittingReview = ref(false)
const reviewForm = reactive({
  score: 5,
  content: '',
  isAnonymous: 0
})

// 问答相关
const questions = ref<QuestionItem[]>([])
const questionPage = ref(1)
const questionTotal = ref(0)
const submittingQuestion = ref(false)
const submittingAnswerId = ref<number | string | null>(null)
const acceptingId = ref<number | string | null>(null)
const questionForm = reactive({ content: '' })
const answerInputs = reactive<Record<string, string>>({})
const recommends = ref<CourseDetail[]>([])

const couponLabel = (c: any) => {
  if (!c) return ''
  
  const price = Number(course.value?.price || 0)
  let savingText = ''
  const type = Number(c.type)
  
  if (type === 1) {
    // 满减券
    const discountAmount = Number(c.discountAmount || 0)
    const threshold = Number(c.thresholdAmount || 0)
    savingText = `减¥${discountAmount}`
    if (threshold > 0) {
      savingText += ` (满¥${threshold})`
    }
    return `${c.name || '满减券'} - ${savingText}`
  }
  
  if (type === 2) {
    // 折扣券
    const rate = Number(c.discountRate || 1)
    const discount = Math.round((1 - rate) * 100)
    const threshold = Number(c.thresholdAmount || 0)
    savingText = `${Math.round(rate * 100)}折`
    if (threshold > 0) {
      savingText += ` (满¥${threshold})`
    }
    // 计算实际节省金额
    if (price > 0) {
      const actualSaving = price - (price * rate)
      savingText += ` 省¥${actualSaving.toFixed(2)}`
    }
    return `${c.name || '折扣券'} - ${savingText}`
  }
  
  return c.name || '优惠券'
}

// 学习记录
const studyRecords = ref<StudyRecord[]>([])

function formatTime(time: string) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

function getLessonFinished(lessonId: number) {
  const record = studyRecords.value.find(r => r.lessonId === lessonId)
  return record && record.isFinished === 1
}

async function load() {
  const id = route.params.id as string
  if (!id) return
  loading.value = true
  try {
    const res = await getCourseDetail(id)
    course.value = {
      ...res.data,
      isFree: Number(res.data.isFree),
      price: Number(res.data.price),
      originalPrice: Number(res.data.originalPrice)
    }
    if (res.data.chapters?.length) activeChapters.value = [res.data.chapters[0].id]
    
    if (userStore.isLoggedIn) {
      const [buyRes, favRes, reviewedRes] = await Promise.all([
        checkBuyCourse(id as any),
        checkFavorite(id as any),
        checkReviewed(id as any)
      ])
      hasBought.value = buyRes.data
      isFavorite.value = favRes.data
      hasReviewed.value = reviewedRes.data

      // 如果已购买，加载学习记录
      if (hasBought.value) {
        const recordsRes = await getCourseStudyRecords(id as any)
        studyRecords.value = recordsRes.data || []
      }
    }

    // 加载评价
    loadReviews()
    // 加载可用优惠券（需登录）
    if (userStore.isLoggedIn) {
      await loadCoupons()
    }
    // 加载问答
    loadQuestions()
    loadRecommends()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadCoupons() {
  if (!userStore.isLoggedIn || !course.value) return
  loadingCoupons.value = true
  try {
    const res = await getAvailableCoupons(course.value.price)
    availableCoupons.value = res.data || []
    console.log('加载到的优惠券:', availableCoupons.value)
  } catch (e) { 
    console.error('加载优惠券失败:', e)
  } finally { 
    loadingCoupons.value = false 
  }
}

function onCouponChange(value: any) {
  console.log('选择优惠券:', value)
  const selectedCoupon = availableCoupons.value.find(c => String(c.userCouponId) === String(value))
  if (selectedCoupon) {
    console.log('选中的优惠券详情:', selectedCoupon)
  }
}

async function loadReviews() {
  if (!course.value) return
  try {
    const res = await getReviewList(course.value.id, reviewPage.value)
    if (reviewPage.value === 1) {
      reviews.value = res.data.records
    } else {
      reviews.value.push(...res.data.records)
    }
    reviewTotal.value = res.data.total
  } catch (e) { console.error(e) }
}

async function loadQuestions() {
  if (!course.value) return
  try {
    const res = await getQuestionList(course.value.id, questionPage.value)
    const list = res.data.records || []
    if (questionPage.value === 1) {
      questions.value = list
    } else {
      questions.value.push(...list)
    }
    questionTotal.value = res.data.total
  } catch (e) { console.error(e) }
}

async function loadMoreReviews() {
  reviewPage.value++
  await loadReviews()
}

async function loadMoreQuestionsFn() {
  questionPage.value++
  await loadQuestions()
}

async function handleBuy() {
  if (!userStore.isLoggedIn) { router.push({ name: 'Login', query: { redirect: route.fullPath } }); return }
  if (!course.value) return
  const payload: any = {
    courseId: course.value.id,
    courseTitle: course.value.title,
    courseCover: course.value.cover,
    teacherName: course.value.teacherName,
    teacherId: course.value.teacherId,
    orgId: course.value.orgId,
    orgName: course.value.orgName,
    originalPrice: course.value.price,
    username: userStore.userInfo?.username,
    couponId: selectedCouponId.value || undefined
  }
  if (course.value.isFree) {
    await createOrder({ ...payload, originalPrice: 0 })
    ElMessage.success('已加入学习')
    hasBought.value = true
    return
  }
  ElMessageBox.confirm(`确定购买「${course.value.title}」？`, '购买确认').then(async () => {
    await createOrder(payload)
    ElMessage.success('订单已创建')
    router.push('/user/orders')
  }).catch(() => {})
}

async function loadRecommends() {
  if (!course.value) return
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 4, orderBy: 'popular', categoryId: course.value.categoryId as any })
    recommends.value = (res.data.records || []).map((c: any) => ({
      id: String(c.id),
      title: c.title,
      cover: c.cover,
      teacherName: c.teacherName,
      studyCount: c.studyCount,
      isFree: Number(c.isFree),
      price: Number(c.price)
    }))
  } catch (e) { console.error(e) }
}

function goStudy() {
  if (!course.value) return
  router.push(`/study/${course.value.id}`)
}

function goRecommend(id: string | number) {
  router.push(`/course/${id}`)
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) { router.push({ name: 'Login', query: { redirect: route.fullPath } }); return }
  if (!course.value) return
  try {
    if (isFavorite.value) {
      await removeFavorite(course.value.id)
      isFavorite.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(course.value.id)
      isFavorite.value = true
      ElMessage.success('收藏成功')
    }
  } catch (e) { console.error(e) }
}

async function submitQuestion() {
  if (!course.value || !questionForm.content.trim()) return
  submittingQuestion.value = true
  try {
    await addQuestion({ courseId: course.value.id, content: questionForm.content.trim() })
    ElMessage.success('提问成功')
    questionForm.content = ''
    questionPage.value = 1
    await loadQuestions()
  } catch (e) { console.error(e) }
  finally { submittingQuestion.value = false }
}

async function submitAnswer(q: QuestionItem) {
  if (!course.value) return
  const content = (answerInputs[q.id as any] || '').trim()
  if (!content) return
  submittingAnswerId.value = q.id
  try {
    await addAnswer({ courseId: course.value.id, questionId: q.id, content })
    ElMessage.success('回答成功')
    answerInputs[q.id as any] = ''
    questionPage.value = 1
    await loadQuestions()
  } catch (e) { console.error(e) }
  finally { submittingAnswerId.value = null }
}

async function doAccept(answerId: number | string) {
  acceptingId.value = answerId
  try {
    await acceptAnswer(answerId)
    ElMessage.success('已采纳该回答')
    questionPage.value = 1
    await loadQuestions()
  } catch (e) { console.error(e) }
  finally { acceptingId.value = null }
}

async function submitReview() {
  if (!course.value) return
  if (reviewForm.score === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  submittingReview.value = true
  try {
    await addReview({
      courseId: course.value.id,
      score: reviewForm.score,
      content: reviewForm.content,
      isAnonymous: reviewForm.isAnonymous
    })
    ElMessage.success('评价成功')
    hasReviewed.value = true
    reviewPage.value = 1
    await loadReviews()
    // 重新加载课程信息以更新评分
    const res = await getCourseDetail(course.value.id)
    course.value = res.data
  } catch (e) { console.error(e) }
  finally { submittingReview.value = false }
}

function handleLessonClick(lesson: LessonInfo) {
  if (hasBought.value || lesson.isFree) {
    router.push(`/study/${course.value?.id}?lesson=${lesson.id}`)
  } else {
    ElMessage.info('购买课程后即可学习')
  }
}

onMounted(() => {
  load()
  loadCoupons()
})
</script>

<style lang="scss" scoped>
.detail-page { min-height: calc(100vh - 90px); }

.header { background: linear-gradient(135deg, #1e1b4b, #312e81); padding: 28px 0;
  .header-inner { max-width: 1100px; margin: 0 auto; padding: 0 16px; display: flex; gap: 24px; }
  .cover { width: 320px; height: 180px; border-radius: 10px; overflow: hidden; flex-shrink: 0; img { width: 100%; height: 100%; object-fit: cover; } }
  .info { color: #fff; flex: 1; }
  h1 { font-size: 20px; margin-bottom: 6px; }
  .sub { font-size: 12px; opacity: 0.8; margin-bottom: 12px; }
  .meta { display: flex; align-items: center; gap: 16px; font-size: 11px; opacity: 0.7; margin-bottom: 20px;
    :deep(.el-rate) { height: auto; }
  }
  .action { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
  .price { font-size: 24px; font-weight: 700; color: #f87171; margin-right: 8px; del { font-size: 12px; color: rgba(255,255,255,0.6); margin-left: 6px; } .final-pay { font-size: 13px; color: #fbbf24; margin-left: 8px; } }
  .coupon-row {
    .coupon-select { width: 180px; }
    :deep(.el-input__wrapper) { background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); box-shadow: none; }
    :deep(.el-input__inner) { color: #fff; &::placeholder { color: rgba(255,255,255,0.6); } }
  }
}

.content { max-width: 1100px; margin: 0 auto; padding: 20px 16px; display: flex; gap: 20px;
  .main { flex: 1; background: #fff; border-radius: 10px; padding: 16px; min-width: 0; }
  .side { width: 260px; flex-shrink: 0; }
}

.recommend-card {
  margin-top: 16px; background: #fff; border-radius: 10px; padding: 16px;
  h4 { font-size: 14px; font-weight: 600; margin-bottom: 12px; padding-left: 8px; border-left: 3px solid var(--primary-color); }
  .rec-item {
    display: flex; gap: 10px; margin-bottom: 12px; cursor: pointer; transition: all 0.2s;
    &:hover { transform: translateX(4px); .title { color: var(--primary-color); } }
    img { width: 90px; height: 60px; border-radius: 6px; object-fit: cover; flex-shrink: 0; }
    .rec-info {
      flex: 1; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden;
      .title { font-size: 13px; color: #333; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
      .meta { font-size: 11px; color: #999; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      .price { font-size: 13px; font-weight: 600; color: #f87171; margin: 0; }
    }
  }
}

.ch-title { display: flex; justify-content: space-between; width: 100%; padding-right: 12px; font-size: 12px; .cnt { font-size: 10px; color: #999; } }
.lesson { display: flex; align-items: center; gap: 10px; padding: 8px 12px; border-bottom: 1px solid #f5f5f5; font-size: 11px; cursor: pointer; transition: background 0.2s;
  &:hover { background: #f8fafc; }
  &:last-child { border: none; }
  .name { flex: 1; }
  .dur { color: #999; }
  .finished { color: #10b981; }
}

.teacher-card { background: #fff; border-radius: 10px; padding: 16px; text-align: center; h4 { font-size: 12px; margin-bottom: 12px; } .name { margin-top: 8px; font-size: 13px; font-weight: 500; } }

// 评价样式
.reviews-section {
  .review-stats {
    padding: 16px; background: #f8fafc; border-radius: 8px; margin-bottom: 16px;
    .avg-score { display: flex; align-items: center; gap: 12px;
      .num { font-size: 32px; font-weight: 700; color: var(--primary-color); }
      .total { font-size: 11px; color: var(--text-muted); }
    }
  }

  .add-review {
    padding: 16px; background: #fff; border: 1px solid #eee; border-radius: 8px; margin-bottom: 16px;
    &.tip { padding: 0; border: none; }
    h4 { font-size: 13px; margin-bottom: 12px; }
    .review-form {
      .row { margin-bottom: 12px; display: flex; align-items: center; gap: 10px;
        &:last-child { justify-content: space-between; }
        .label { font-size: 12px; color: var(--text-secondary); }
      }
    }
  }

  .review-list {
    .review-item { display: flex; gap: 12px; padding: 14px 0; border-bottom: 1px solid #f0f0f0;
      &:last-child { border: none; }
    }
    .review-body { flex: 1; }
    .review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px;
      .name { font-size: 12px; font-weight: 500; }
    }
    .review-content { font-size: 12px; color: #666; line-height: 1.6; margin-bottom: 6px; }
    .review-time { font-size: 10px; color: var(--text-muted); }
    .load-more { text-align: center; padding: 10px 0; }
  }
}
</style>
