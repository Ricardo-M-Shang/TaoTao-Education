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


