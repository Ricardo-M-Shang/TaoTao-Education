/**
 * 创建订单参数
 */
export interface CreateOrderParams {
  courseId: number | string
  courseTitle: string
  courseCover: string
  teacherName: string
  teacherId?: number | string
  orgId?: number | string
  orgName?: string
  originalPrice: number
  couponId?: number | string
  username?: string
}

/**
 * 订单信息
 */
export interface OrderInfo {
  id: number
  orderNo: string
  userId: number
  courseId: number
  courseTitle: string
  courseCover: string
  teacherName: string
  orgId?: number
  orgName?: string
  originalPrice: number
  discountAmount: number
  payAmount: number
  orgIncome?: number
  platformIncome?: number
  payType: number
  status: number
  payTime: string
  expireTime: string
  createTime: string
}

/**
 * 订单列表返回结果
 */
export interface OrderListResult {
  current: number
  size: number
  total: number
  pages: number
  records: OrderInfo[]
}

