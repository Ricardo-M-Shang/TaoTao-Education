import { request, ApiResponse } from '@/utils/request'

export function receiveCoupon(couponId: number | string): Promise<ApiResponse<void>> {
  return request({
    url: '/order/coupon/receive',
    method: 'post',
    data: { couponId }
  })
}

export function getMyCoupons(params: { status?: number; pageNum?: number; pageSize?: number } = {}): Promise<ApiResponse<any>> {
  return request({
    url: '/order/coupon/my',
    method: 'get',
    params
  })
}

export function getAvailableCoupons(orderAmount: number): Promise<ApiResponse<any[]>> {
  return request({
    url: '/order/coupon/available',
    method: 'get',
    params: { orderAmount }
  })
}


