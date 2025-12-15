import { request, ApiResponse } from '@/utils/request'
import type { LoginParams, RegisterParams, LoginResult, UserInfo, OrgOption } from '@/types/user'

/**
 * 用户登录
 */
export function login(data: LoginParams): Promise<ApiResponse<LoginResult>> {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export function register(data: RegisterParams): Promise<ApiResponse<void>> {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 */
export function getUserInfo(): Promise<ApiResponse<UserInfo>> {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data: {
  nickname?: string
  email?: string
  avatar?: string
  gender?: number
  province?: string
  city?: string
  signature?: string
}): Promise<ApiResponse<void>> {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 */
export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<void>> {
  return request({
    url: '/user/password/change',
    method: 'post',
    data
  })
}

/**
 * 退出登录
 */
export function logout(): Promise<ApiResponse<void>> {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 获取机构下拉列表
 */
export function getOrgOptions(): Promise<ApiResponse<OrgOption[]>> {
  return request({
    url: '/user/orgs',
    method: 'get'
  })
}

