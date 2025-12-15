import { request, ApiResponse } from '@/utils/request'

export interface QuestionItem {
  id: number | string
  courseId: number | string
  userId: number | string
  username?: string
  nickname?: string
  content: string
  answerCount?: number
  answers?: AnswerItem[]
  createTime?: string
}

export interface AnswerItem {
  id: number | string
  questionId: number | string
  courseId: number | string
  userId: number | string
  nickname?: string
  username?: string
  content: string
  accepted?: number
  createTime?: string
}

export interface QuestionPage {
  current: number
  size: number
  total: number
  records: QuestionItem[]
}

export function addQuestion(data: { courseId: number | string; content: string }): Promise<ApiResponse<void>> {
  return request({ url: '/course/qa/question', method: 'post', data })
}

export function addAnswer(data: { questionId: number | string; courseId: number | string; content: string }): Promise<ApiResponse<void>> {
  return request({ url: '/course/qa/answer', method: 'post', data })
}

export function getQuestionList(courseId: number | string, pageNum = 1, pageSize = 10): Promise<ApiResponse<QuestionPage>> {
  return request({
    url: `/course/qa/question/${courseId}`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function acceptAnswer(answerId: number | string): Promise<ApiResponse<void>> {
  return request({
    url: `/course/qa/answer/${answerId}/accept`,
    method: 'post'
  })
}


