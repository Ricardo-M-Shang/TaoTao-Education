import { request, ApiResponse } from '@/utils/request'
import type { CourseListParams, CourseListResult } from '@/types/course'
import type { OrderListResult } from '@/types/order'

export interface OrgTeacherOption {
  id: number | string
  name: string
}

export function getOrgCourseList(params: CourseListParams): Promise<ApiResponse<CourseListResult>> {
  return request({
    url: '/course/org/list',
    method: 'get',
    params
  })
}

export function approveCourse(courseId: number | string, remark?: string): Promise<ApiResponse<void>> {
  return request({
    url: `/course/org/approve/${courseId}`,
    method: 'post',
    data: remark ? { remark } : {}
  })
}

export function rejectCourse(courseId: number | string, remark?: string): Promise<ApiResponse<void>> {
  return request({
    url: `/course/org/reject/${courseId}`,
    method: 'post',
    data: remark ? { remark } : {}
  })
}

export function offlineCourseOrg(courseId: number | string): Promise<ApiResponse<void>> {
  return request({
    url: `/course/org/offline/${courseId}`,
    method: 'post'
  })
}

export function getOrgOrders(params: { status?: number; teacherId?: number | string; pageNum: number; pageSize: number }): Promise<ApiResponse<OrderListResult>> {
  return request({
    url: '/order/org/list',
    method: 'get',
    params
  })
}

export function getOrgStats(): Promise<ApiResponse<{
  totalIncome: number
  todayIncome: number
  monthIncome: number
  paidOrders: number
}>> {
  return request({
    url: '/order/org/stats',
    method: 'get'
  })
}

export function getOrgTrend(params: { days?: number } = {}): Promise<ApiResponse<Array<{ day: string; income: number; paidOrders: number }>>> {
  return request({
    url: '/order/org/trend',
    method: 'get',
    params
  })
}

export function getOrgTeachers(): Promise<ApiResponse<OrgTeacherOption[]>> {
  return request({
    url: '/course/org/teachers',
    method: 'get'
  })
}


