/**
 * 课程列表查询参数
 */
export interface CourseListParams {
  keyword?: string
  categoryId?: number
  type?: number
  isFree?: number
  orderBy?: string
  pageNum: number
  pageSize: number
}

/**
 * 课程列表项
 */
export interface CourseListItem {
  id: number
  title: string
  subtitle: string
  cover: string
  teacherName: string
  categoryName: string
  type: number
  price: number
  originalPrice: number
  isFree: number
  lessonCount: number
  studyCount: number
  score: number
  createTime: string
}

/**
 * 课程列表返回结果
 */
export interface CourseListResult {
  current: number
  size: number
  total: number
  pages: number
  records: CourseListItem[]
}

/**
 * 章节信息
 */
export interface ChapterInfo {
  id: number
  title: string
  sort: number
  lessons: LessonInfo[]
}

/**
 * 课时信息
 */
export interface LessonInfo {
  id: number
  title: string
  videoUrl: string
  duration: number
  isFree: number
  sort: number
  type: number
}

/**
 * 课程详情
 */
export interface CourseDetail {
  id: number
  title: string
  subtitle: string
  cover: string
  description: string
  content: string
  teacherId: number
  teacherName: string
  categoryId: number
  categoryName: string
  type: number
  price: number
  originalPrice: number
  isFree: number
  lessonCount: number
  studyCount: number
  score: number
  totalDuration: number
  createTime: string
  chapters: ChapterInfo[]
}

/**
 * 分类树
 */
export interface CategoryTree {
  id: number
  name: string
  parentId: number
  level: number
  icon?: string
  children?: CategoryTree[]
}

