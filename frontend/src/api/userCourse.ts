import { request, ApiResponse } from '@/utils/request'

/**
 * 用户课程信息
 */
export interface UserCourseInfo {
  id: number
  courseId: number
  courseTitle: string
  courseCover: string
  teacherName: string
  progress: number
  lastStudyTime: string
  isFinished: number
  createTime: string
}

/**
 * 获取用户已购课程列表
 */
export function getUserCourses(): Promise<ApiResponse<UserCourseInfo[]>> {
  return request({
    url: '/order/user-course/list',
    method: 'get'
  })
}

/**
 * 更新学习进度
 */
export function updateProgress(courseId: number, progress: number): Promise<ApiResponse<void>> {
  return request({
    url: '/order/user-course/progress',
    method: 'post',
    params: { courseId, progress }
  })
}

