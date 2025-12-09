import { request, ApiResponse } from '@/utils/request'

/**
 * 学习记录
 */
export interface StudyRecord {
  id: number
  courseId: number
  lessonId: number
  chapterId: number
  duration: number
  progress: number
  isFinished: number
  updateTime: string
}

/**
 * 更新学习进度参数
 */
export interface UpdateProgressParams {
  courseId: number | string
  lessonId: number | string
  chapterId?: number | string
  duration?: number
  progress?: number
}

/**
 * 学习统计数据
 */
export interface StudyStatistics {
  totalStudyTime: number      // 累计学习时长(分钟)
  totalCourses: number        // 学习课程数
  finishedCourses: number     // 已完成课程数
  studyDays: number           // 学习天数
  weeklyStudyTime: number[]   // 最近7天学习时长
}

/**
 * 最近学习记录
 */
export interface RecentStudyRecord {
  id: number
  courseId: number
  courseTitle: string
  courseCover: string
  lessonId: number
  lessonTitle: string
  progress: number
  updateTime: string
}

/**
 * 更新学习进度
 */
export function updateStudyProgress(data: UpdateProgressParams): Promise<ApiResponse<void>> {
  return request({
    url: '/course/study/progress',
    method: 'post',
    data
  })
}

/**
 * 获取课程学习记录
 */
export function getCourseStudyRecords(courseId: number | string): Promise<ApiResponse<StudyRecord[]>> {
  return request({
    url: `/course/study/records/${courseId}`,
    method: 'get'
  })
}

/**
 * 获取课时学习记录
 */
export function getLessonStudyRecord(lessonId: number | string): Promise<ApiResponse<StudyRecord>> {
  return request({
    url: `/course/study/record/lesson/${lessonId}`,
    method: 'get'
  })
}

/**
 * 获取课程学习进度
 */
export function getCourseProgress(courseId: number | string): Promise<ApiResponse<number>> {
  return request({
    url: `/course/study/progress/${courseId}`,
    method: 'get'
  })
}

/**
 * 获取学习统计数据
 */
export function getStudyStatistics(): Promise<ApiResponse<StudyStatistics>> {
  return request({
    url: '/course/study/statistics',
    method: 'get'
  })
}

/**
 * 获取最近学习记录
 */
export function getRecentStudyRecords(limit: number = 5): Promise<ApiResponse<RecentStudyRecord[]>> {
  return request({
    url: '/course/study/recent',
    method: 'get',
    params: { limit }
  })
}

