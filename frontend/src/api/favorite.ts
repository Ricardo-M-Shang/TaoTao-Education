import { request, ApiResponse } from '@/utils/request'

/**
 * 收藏信息
 */
export interface FavoriteInfo {
  id: number
  courseId: number
  courseTitle: string
  courseCover: string
  teacherName: string
  price: number
  isFree: number
  studyCount: number
  score: number
  createTime: string
}

/**
 * 收藏列表结果
 */
export interface FavoriteListResult {
  current: number
  size: number
  total: number
  pages: number
  records: FavoriteInfo[]
}

/**
 * 添加收藏
 */
export function addFavorite(courseId: number | string): Promise<ApiResponse<void>> {
  return request({
    url: `/course/favorite/add/${courseId}`,
    method: 'post'
  })
}

/**
 * 取消收藏
 */
export function removeFavorite(courseId: number | string): Promise<ApiResponse<void>> {
  return request({
    url: `/course/favorite/remove/${courseId}`,
    method: 'delete'
  })
}

/**
 * 检查是否已收藏
 */
export function checkFavorite(courseId: number | string): Promise<ApiResponse<boolean>> {
  return request({
    url: `/course/favorite/check/${courseId}`,
    method: 'get'
  })
}

/**
 * 获取收藏列表
 */
export function getFavoriteList(pageNum = 1, pageSize = 10): Promise<ApiResponse<FavoriteListResult>> {
  return request({
    url: '/course/favorite/list',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

