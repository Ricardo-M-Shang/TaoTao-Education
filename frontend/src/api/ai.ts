import { request, type ApiResponse } from '@/utils/request'

/**
 * 推荐课程信息
 */
export interface RecommendCourse {
  id: string | number
  title: string
  subtitle: string
  cover: string
  description: string
  teacherName: string
  categoryName: string
  price: number
  isFree: number
  studyCount: number
  score: number
  reason: string
  recommendScore: number
}

/**
 * 推荐结果
 */
export interface RecommendResult {
  courses: RecommendCourse[]
  generatedAt: string
  fromCache: boolean
  summary: string
}

/**
 * 获取个性化推荐
 */
export function getRecommendations(params?: {
  count?: number
  includeLearnedCourses?: boolean
  categoryId?: number
}): Promise<ApiResponse<RecommendResult>> {
  return request<RecommendResult>({
    url: '/ai/recommend',
    method: 'get',
    params
  })
}

/**
 * 刷新推荐
 */
export function refreshRecommendations(data?: {
  count?: number
  includeLearnedCourses?: boolean
  categoryId?: number
}): Promise<ApiResponse<RecommendResult>> {
  return request<RecommendResult>({
    url: '/ai/recommend/refresh',
    method: 'post',
    data
  })
}

/**
 * 获取热门推荐
 */
export function getPopularRecommendations(count?: number): Promise<ApiResponse<RecommendResult>> {
  return request<RecommendResult>({
    url: '/ai/recommend/popular',
    method: 'get',
    params: { count }
  })
}

/**
 * AI对话请求
 */
export interface AIChatRequest {
  message: string
  role: number // 1-学生, 2-老师
}

/**
 * AI对话响应
 */
export interface AIChatResponse {
  reply: string
}

/**
 * 与AI助手对话
 */
export function chatWithAI(data: AIChatRequest): Promise<ApiResponse<AIChatResponse>> {
  return request<AIChatResponse>({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

