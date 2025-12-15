import { request, ApiResponse } from '@/utils/request'

/**
 * 笔记信息
 */
export interface NoteInfo {
  id: number
  courseId: number
  lessonId: number
  lessonTitle?: string
  content: string
  videoTime?: number  // 视频时间点（秒）
  createTime: string
  updateTime: string
}

/**
 * 笔记列表结果
 */
export interface NoteListResult {
  current: number
  size: number
  total: number
  pages: number
  records: NoteInfo[]
}

/**
 * 添加/更新笔记参数
 */
export interface SaveNoteParams {
  courseId: number
  lessonId: number
  content: string
  videoTime?: number
}

/**
 * 获取课程笔记列表
 */
export function getCourseNotes(courseId: number, pageNum = 1, pageSize = 20): Promise<ApiResponse<NoteListResult>> {
  return request({
    url: `/course/note/list/${courseId}`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 获取课时笔记
 */
export function getLessonNotes(lessonId: number): Promise<ApiResponse<NoteInfo[]>> {
  return request({
    url: `/course/note/lesson/${lessonId}`,
    method: 'get'
  })
}

/**
 * 保存笔记
 */
export function saveNote(data: SaveNoteParams): Promise<ApiResponse<number>> {
  return request({
    url: '/course/note/save',
    method: 'post',
    data
  })
}

/**
 * 更新笔记
 */
export function updateNote(id: number, content: string): Promise<ApiResponse<void>> {
  return request({
    url: `/course/note/update/${id}`,
    method: 'put',
    data: { content }
  })
}

/**
 * 删除笔记
 */
export function deleteNote(id: number): Promise<ApiResponse<void>> {
  return request({
    url: `/course/note/${id}`,
    method: 'delete'
  })
}

