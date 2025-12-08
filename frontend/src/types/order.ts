/**
 * 创建订单参数
 */
export interface CreateOrderParams {
  courseId: number
  courseTitle: string
  courseCover: string
  teacherName: string
  originalPrice: number
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
  originalPrice: number
  discountAmount: number
  payAmount: number
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

