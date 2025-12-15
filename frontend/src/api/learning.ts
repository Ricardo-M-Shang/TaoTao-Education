import { request, ApiResponse } from '@/utils/request'

// ========== 学习记录相关 ==========

/**
 * 学习进度记录参数
 */
export interface StudyProgressParams {
  courseId: number
  lessonId: number
  studyDuration?: number
  videoDuration?: number
  lastPosition: number
  isCompleted?: number
  deviceType?: string
}

/**
 * 学习记录
 */
export interface StudyRecord {
  id: number
  courseId: number
  courseTitle: string
  lessonId: number
  lessonTitle: string
  studyDuration: number
  videoDuration: number
  lastPosition: number
  progressPercent: number
  isCompleted: boolean
  studyDate: string
  deviceType: string
  createTime: string
}

/**
 * 记录学习进度
 */
export function recordStudyProgress(data: StudyProgressParams): Promise<ApiResponse<void>> {
  return request({
    url: '/learning/study/progress',
    method: 'post',
    data
  })
}

/**
 * 获取学习记录列表
 */
export function getStudyRecords(params: {
  courseId?: number
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<{
  records: StudyRecord[]
  total: number
  current: number
  size: number
}>> {
  return request({
    url: '/learning/study/records',
    method: 'get',
    params
  })
}

/**
 * 获取最近学习记录
 */
export function getRecentStudyRecords(limit?: number): Promise<ApiResponse<StudyRecord[]>> {
  return request({
    url: '/learning/study/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取课程学习进度
 */
export function getCourseProgress(courseId: number): Promise<ApiResponse<number>> {
  return request({
    url: `/learning/study/course/${courseId}/progress`,
    method: 'get'
  })
}

/**
 * 获取课时学习进度
 */
export function getLessonProgress(lessonId: number): Promise<ApiResponse<any>> {
  return request({
    url: `/learning/study/lesson/${lessonId}/progress`,
    method: 'get'
  })
}

// ========== 学习统计相关 ==========

/**
 * 课程学习统计
 */
export interface LearningStats {
  courseId: number
  courseTitle: string
  courseCover: string
  teacherName: string
  totalLessons: number
  completedLessons: number
  totalDuration: number
  studyDuration: number
  completionRate: number
  firstStudyTime: string
  lastStudyTime: string
  studyDays: number
  avgDailyDuration: number
  isFinished: boolean
  status: 'not_started' | 'learning' | 'completed'
}

/**
 * 获取课程学习统计
 */
export function getCourseStats(courseId: number): Promise<ApiResponse<LearningStats>> {
  return request({
    url: `/learning/stats/course/${courseId}`,
    method: 'get'
  })
}

/**
 * 获取用户所有课程学习统计
 */
export function getUserCourseStats(): Promise<ApiResponse<LearningStats[]>> {
  return request({
    url: '/learning/stats/courses',
    method: 'get'
  })
}

/**
 * 获取正在学习的课程
 */
export function getLearningCourses(): Promise<ApiResponse<LearningStats[]>> {
  return request({
    url: '/learning/stats/learning',
    method: 'get'
  })
}

/**
 * 获取已完成的课程
 */
export function getCompletedCourses(): Promise<ApiResponse<LearningStats[]>> {
  return request({
    url: '/learning/stats/completed',
    method: 'get'
  })
}

/**
 * 获取课程完成率分布
 */
export function getCompletionDistribution(): Promise<ApiResponse<Array<{
  rangeName: string
  count: number
}>>> {
  return request({
    url: '/learning/stats/completion-distribution',
    method: 'get'
  })
}

// ========== 学习报告相关 ==========

/**
 * 学习报告
 */
export interface LearningReport {
  period: string
  startDate: string
  endDate: string
  totalStudyDuration: number
  totalStudyDays: number
  studyCourseCount: number
  completedCourseCount: number
  avgDailyDuration: number
  currentStreak: number
  durationTrend: Array<{ studyDate: string; duration: number }>
  progressTrend: Array<any>
  completionDistribution: Array<{ rangeName: string; count: number }>
  timeDistribution: Array<{ timeSlot: string; name: string; duration: number }>
  recentCourses: LearningStats[]
  activeCourses: LearningStats[]
  newCompletedCourses: LearningStats[]
  level: number
  nextLevelPoints: number
  suggestions: string[]
}

/**
 * 用户学习总统计
 */
export interface UserLearningStats {
  userId: number
  totalCourses: number
  completedCourses: number
  totalStudyDuration: number
  totalStudyDays: number
  avgDailyDuration: number
  longestStreak: number
  currentStreak: number
  lastStudyDate: string
  levelScore: number
  certificatesCount: number
}

/**
 * 获取学习报告
 */
export function getLearningReport(params: {
  period?: 'week' | 'month' | 'year'
  courseId?: number
  type?: 'summary' | 'detail'
}): Promise<ApiResponse<LearningReport>> {
  return request({
    url: '/learning/report',
    method: 'get',
    params
  })
}

/**
 * 获取学习时长趋势
 */
export function getStudyDurationTrend(period: string = 'week'): Promise<ApiResponse<Array<{
  studyDate: string
  duration: number
}>>> {
  return request({
    url: '/learning/report/duration-trend',
    method: 'get',
    params: { period }
  })
}

/**
 * 获取学习时段分布
 */
export function getStudyTimeDistribution(period: string = 'week'): Promise<ApiResponse<Array<{
  timeSlot: string
  name: string
  duration: number
}>>> {
  return request({
    url: '/learning/report/time-distribution',
    method: 'get',
    params: { period }
  })
}

/**
 * 获取用户学习总统计
 */
export function getUserLearningStats(): Promise<ApiResponse<UserLearningStats>> {
  return request({
    url: '/learning/report/user-stats',
    method: 'get'
  })
}

/**
 * 获取学习建议
 */
export function getLearningAdvice(): Promise<ApiResponse<string[]>> {
  return request({
    url: '/learning/report/advice',
    method: 'get'
  })
}

/**
 * 获取用户学习等级
 */
export function getUserLevel(): Promise<ApiResponse<number>> {
  return request({
    url: '/learning/report/level',
    method: 'get'
  })
}

/**
 * 更新用户学习统计
 */
export function updateUserStats(): Promise<ApiResponse<void>> {
  return request({
    url: '/learning/report/user-stats/update',
    method: 'post'
  })
}
