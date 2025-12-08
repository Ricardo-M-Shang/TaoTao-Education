/**
 * 登录参数
 */
export interface LoginParams {
  username: string
  password: string
}

/**
 * 注册参数
 */
export interface RegisterParams {
  username: string
  password: string
  phone?: string
  email?: string
  nickname?: string
}

/**
 * 登录返回结果
 */
export interface LoginResult {
  token: string
  userId: number
  username: string
  nickname: string
  avatar: string
  role: number
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  phone?: string
  email?: string
  nickname: string
  avatar?: string
  gender?: number
  province?: string
  city?: string
  signature?: string
  role: number
  createTime?: string
  lastLoginTime?: string
}

