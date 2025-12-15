import { request, ApiResponse } from '@/utils/request'
import type { CreateOrderParams, OrderInfo, OrderListResult } from '@/types/order'

/**
 * 创建订单
 */
export function createOrder(data: CreateOrderParams): Promise<ApiResponse<string>> {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(orderNo: string): Promise<ApiResponse<OrderInfo>> {
  return request({
    url: `/order/detail/${orderNo}`,
    method: 'get'
  })
}

/**
 * 获取用户订单列表
 */
export function getOrderList(params: { status?: number; pageNum: number; pageSize: number }): Promise<ApiResponse<OrderListResult>> {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

/**
 * 取消订单
 */
export function cancelOrder(orderNo: string): Promise<ApiResponse<void>> {
  return request({
    url: `/order/cancel/${orderNo}`,
    method: 'post'
  })
}

/**
 * 支付订单
 */
export function payOrder(data: { orderNo: string; payType: number }): Promise<ApiResponse<void>> {
  return request({
    url: '/order/pay',
    method: 'post',
    data
  })
}

/**
 * 查询可用优惠券
 */
export function getAvailableCoupons(orderAmount: number): Promise<ApiResponse<any[]>> {
  return request({
    url: '/order/coupon/available',
    method: 'get',
    params: { orderAmount }
  })
}

/**
 * 检查是否已购买课程
 */
export function checkBuyCourse(courseId: number | string): Promise<ApiResponse<boolean>> {
  return request({
    url: `/order/check/${courseId}`,
    method: 'get'
  })
}

