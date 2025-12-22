import { request, ApiResponse } from '@/utils/request'

export interface OpsOrderOverview {
  totalIncome: number
  todayIncome: number
  monthIncome: number
  paidOrders: number
}

export interface OpsCourseOverview {
  total: number
  pending: number
  published: number
  offline: number
}

export interface OpsUserOverview {
  total: number
  students: number
  teachers: number
  orgs: number
  ops: number
}

export function getOpsOrderOverview(): Promise<ApiResponse<OpsOrderOverview>> {
  return request({
    url: '/ops/order/overview',
    method: 'get'
  })
}

export function getOpsOrderTrend(params: { days?: number } = {}): Promise<ApiResponse<Array<{ day: string; income: number; paidOrders: number }>>> {
  return request({
    url: '/ops/order/trend',
    method: 'get',
    params
  })
}

export function getOpsCourseOverview(): Promise<ApiResponse<OpsCourseOverview>> {
  return request({
    url: '/course/ops/overview',
    method: 'get'
  })
}

export function getOpsUserOverview(): Promise<ApiResponse<OpsUserOverview>> {
  return request({
    url: '/user/ops/overview',
    method: 'get'
  })
}

// Coupon Management
export function getCouponList(params: any): Promise<ApiResponse<any>> {
  return request({
    url: '/order/ops/coupon/list',
    method: 'get',
    params
  })
}

export function createCoupon(data: any): Promise<ApiResponse<void>> {
  return request({
    url: '/order/ops/coupon/create',
    method: 'post',
    data
  })
}

export function updateCouponStatus(id: number | string, status: number): Promise<ApiResponse<void>> {
  return request({
    url: `/order/ops/coupon/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function deleteCoupon(id: number | string): Promise<ApiResponse<void>> {
  return request({
    url: `/order/ops/coupon/${id}`,
    method: 'delete'
  })
}


