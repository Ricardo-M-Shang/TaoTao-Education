import { request, ApiResponse } from '@/utils/request'
import type { CourseListParams, CourseListResult, CourseDetail, CategoryTree } from '@/types/course'

/**
 * 获取课程列表
 */
export function getCourseList(params: CourseListParams): Promise<ApiResponse<CourseListResult>> {
  return request({
    url: '/course/list',
    method: 'get',
    params
  })
}

/**
 * 获取课程详情
 */
export function getCourseDetail(courseId: number): Promise<ApiResponse<CourseDetail>> {
  return request({
    url: `/course/detail/${courseId}`,
    method: 'get'
  })
}

/**
 * 获取分类树
 */
export function getCategoryTree(): Promise<ApiResponse<CategoryTree[]>> {
  return request({
    url: '/course/category/tree',
    method: 'get'
  })
}

