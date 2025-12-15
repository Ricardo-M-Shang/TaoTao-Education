import { request, ApiResponse } from '@/utils/request'

/**
 * 评价信息
 */
export interface ReviewInfo {
  id: number
  courseId: number
  userId: number
  nickname: string
  avatar: string
  score: number
  content: string
  isAnonymous: number
  createTime: string
}

/**
 * 评价列表结果
 */
export interface ReviewListResult {
  current: number
  size: number
  total: number
  pages: number
  records: ReviewInfo[]
}

/**
 * 添加评价参数
 */
export interface AddReviewParams {
  courseId: number | string
  score: number
  content?: string
  isAnonymous?: number
}

/**
 * 添加评价
 */
export function addReview(data: AddReviewParams): Promise<ApiResponse<void>> {
  return request({
    url: '/course/review/add',
    method: 'post',
    data
  })
}

/**
 * 获取课程评价列表
 */
export function getReviewList(courseId: number | string, pageNum = 1, pageSize = 10): Promise<ApiResponse<ReviewListResult>> {
  return request({
    url: `/course/review/list/${courseId}`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 检查是否已评价
 */
export function checkReviewed(courseId: number | string): Promise<ApiResponse<boolean>> {
  return request({
    url: `/course/review/check/${courseId}`,
    method: 'get'
  })
}

/**
 * 删除评价
 */
export function deleteReview(reviewId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/course/review/${reviewId}`,
    method: 'delete'
  })
}

