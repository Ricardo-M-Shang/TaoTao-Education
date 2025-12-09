import { request, ApiResponse } from '@/utils/request'
import type { CourseListParams, CourseListResult } from '@/types/course'

/**
 * 讲师课程创建
 */
export function createCourse(data: any): Promise<ApiResponse<number>> {
  return request({
    url: '/course/teacher/create',
    method: 'post',
    data
  })
}

/**
 * 讲师课程更新
 */
export function updateCourse(courseId: number, data: any): Promise<ApiResponse<void>> {
  return request({
    url: `/course/teacher/update/${courseId}`,
    method: 'put',
    data
  })
}

/**
 * 讲师课程发布
 */
export function publishCourse(courseId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/course/teacher/publish/${courseId}`,
    method: 'post'
  })
}

/**
 * 讲师课程下架
 */
export function offlineCourse(courseId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/course/teacher/offline/${courseId}`,
    method: 'post'
  })
}

/**
 * 讲师课程列表（含草稿/发布/下架）
 */
export function getTeacherCourseList(params: CourseListParams): Promise<ApiResponse<CourseListResult>> {
  return request({
    url: '/course/teacher/list',
    method: 'get',
    params
  })
}

/**
 * 讲师收益统计
 */
export interface TeacherStats {
  totalIncome: number
  todayIncome: number
  monthIncome: number
  paidOrders: number
  studentCount: number
}

export function getTeacherStats(): Promise<ApiResponse<TeacherStats>> {
  return request({
    url: '/order/teacher/stats',
    method: 'get'
  })
}

/**
 * 讲师查看课程学员列表
 */
export interface TeacherStudent {
  userId: number
  username: string
  nickname?: string
  courseId: number
  courseTitle: string
  progress: number
  isFinished: number
  lastStudyTime: string
  payTime: string
}

export function getCourseStudents(courseId: number): Promise<ApiResponse<TeacherStudent[]>> {
  return request({
    url: `/order/teacher/students/${courseId}`,
    method: 'get'
  })
}

// 章节与课时管理
export interface ChapterPayload {
  id?: number | string
  courseId: number | string
  title: string
  sort?: number
}

export function createChapter(data: ChapterPayload): Promise<ApiResponse<number>> {
  return request({ url: '/course/teacher/chapter', method: 'post', data })
}

export function updateChapter(id: number, data: ChapterPayload): Promise<ApiResponse<void>> {
  return request({ url: `/course/teacher/chapter/${id}`, method: 'put', data })
}

export function deleteChapter(id: number): Promise<ApiResponse<void>> {
  return request({ url: `/course/teacher/chapter/${id}`, method: 'delete' })
}

export interface LessonPayload {
  id?: number | string
  courseId: number | string
  chapterId: number | string
  title: string
  videoUrl?: string
  duration?: number
  isFree?: number
  sort?: number
  type?: number
}

export function createLesson(data: LessonPayload): Promise<ApiResponse<number>> {
  return request({ url: '/course/teacher/lesson', method: 'post', data })
}

export function updateLesson(id: number, data: LessonPayload): Promise<ApiResponse<void>> {
  return request({ url: `/course/teacher/lesson/${id}`, method: 'put', data })
}

export function deleteLesson(id: number): Promise<ApiResponse<void>> {
  return request({ url: `/course/teacher/lesson/${id}`, method: 'delete' })
}

