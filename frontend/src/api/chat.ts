import { request, ApiResponse } from '@/utils/request'

// ========== 类型定义 ==========

/**
 * 聊天室
 */
export interface ChatRoom {
  id: number
  name: string
  description: string
  cover: string
  courseId: number
  courseTitle: string
  creatorId: number
  creatorName: string
  creatorAvatar: string
  memberCount: number
  maxMembers: number
  status: number
  announcement: string
  needApproval: number
  createTime: string
  latestMessage: string
  latestMessageTime: string
  unreadCount: number
  isPinned: number
  isCreator: boolean
  myRole: number
}

/**
 * 聊天消息
 */
export interface ChatMessage {
  id: number
  roomId: number
  senderId: number
  senderName: string
  senderAvatar: string
  senderRole: number
  messageType: number // 1-文本 2-图片 3-文件 4-系统消息
  content: string
  attachmentUrl?: string
  attachmentName?: string
  attachmentSize?: number
  replyToId?: number
  replyToContent?: string
  mentionedUserIds?: string
  createTime: string
  isMine: boolean
}

/**
 * 聊天室成员
 */
export interface ChatMember {
  id: number
  userId: number
  nickname: string
  avatar: string
  role: number // 1-普通成员 2-管理员 3-创建者
  status: number // 0-已退出 1-正常 2-已禁言
  joinTime: string
  lastActiveTime: string
  isOnline: boolean
}

/**
 * 聊天室邀请
 */
export interface ChatInvitation {
  id: number
  roomId: number
  roomName: string
  inviterId: number
  inviterName: string
  inviterAvatar: string
  status: number // 0-待处理 1-已接受 2-已拒绝 3-已过期
  message: string
  createTime: string
  expireTime: string
}

/**
 * 课程学员（用于邀请）
 */
export interface CourseStudent {
  userId: number
  username: string
  nickname: string
  avatar: string
  inRoom: boolean
  invited: boolean
}

/**
 * 创建聊天室参数
 */
export interface CreateRoomParams {
  name: string
  description?: string
  courseId: number
  cover?: string
  maxMembers?: number
  needApproval?: number
  announcement?: string
}

/**
 * 发送消息参数
 */
export interface SendMessageParams {
  roomId: number
  messageType?: number
  content: string
  attachmentUrl?: string
  attachmentName?: string
  attachmentSize?: number
  replyToId?: number
  mentionedUserIds?: string
}

/**
 * 邀请成员参数
 */
export interface InviteMembersParams {
  roomId: number
  inviteeIds: number[]
  message?: string
}

// ========== 聊天室 API ==========

/**
 * 创建聊天室
 */
export function createChatRoom(data: CreateRoomParams): Promise<ApiResponse<number>> {
  return request({
    url: '/chat/rooms',
    method: 'post',
    data
  })
}

/**
 * 获取聊天室详情
 */
export function getChatRoomDetail(roomId: string | number): Promise<ApiResponse<ChatRoom>> {
  return request({
    url: `/chat/rooms/${roomId}`,
    method: 'get'
  })
}

/**
 * 获取我加入的聊天室列表
 */
export function getMyChatRooms(): Promise<ApiResponse<ChatRoom[]>> {
  return request({
    url: '/chat/rooms/my',
    method: 'get'
  })
}

/**
 * 获取我创建的聊天室列表
 */
export function getCreatedChatRooms(): Promise<ApiResponse<ChatRoom[]>> {
  return request({
    url: '/chat/rooms/created',
    method: 'get'
  })
}

/**
 * 根据课程获取聊天室
 */
export function getChatRoomByCourse(courseId: number): Promise<ApiResponse<ChatRoom>> {
  return request({
    url: `/chat/rooms/course/${courseId}`,
    method: 'get'
  })
}

/**
 * 更新聊天室信息
 */
export function updateChatRoom(roomId: string | number, data: CreateRoomParams): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/rooms/${roomId}`,
    method: 'put',
    data
  })
}

/**
 * 删除聊天室
 */
export function deleteChatRoom(roomId: string | number): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/rooms/${roomId}`,
    method: 'delete'
  })
}

/**
 * 更新聊天室公告
 */
export function updateChatRoomAnnouncement(roomId: string | number, announcement: string): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/rooms/${roomId}/announcement`,
    method: 'put',
    data: announcement,
    headers: { 'Content-Type': 'text/plain' }
  })
}

/**
 * 获取课程学员列表（用于邀请）
 */
export function getCourseStudents(roomId: string | number): Promise<ApiResponse<CourseStudent[]>> {
  return request({
    url: `/chat/rooms/${roomId}/students`,
    method: 'get'
  })
}

// ========== 成员 API ==========

/**
 * 获取聊天室成员列表
 */
export function getChatRoomMembers(roomId: string | number): Promise<ApiResponse<ChatMember[]>> {
  return request({
    url: `/chat/members/room/${roomId}`,
    method: 'get'
  })
}

/**
 * 退出聊天室
 */
export function leaveChatRoom(roomId: string | number): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/members/room/${roomId}/leave`,
    method: 'post'
  })
}

/**
 * 移除成员
 */
export function removeChatMember(roomId: string | number, targetUserId: string | number): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/members/room/${roomId}/user/${targetUserId}`,
    method: 'delete'
  })
}

/**
 * 设置/取消管理员
 */
export function setChatAdmin(roomId: string | number, targetUserId: string | number, isAdmin: boolean): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/members/room/${roomId}/user/${targetUserId}/admin`,
    method: 'post',
    params: { isAdmin }
  })
}

/**
 * 禁言/解禁成员
 */
export function muteChatMember(roomId: string | number, targetUserId: string | number, mute: boolean): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/members/room/${roomId}/user/${targetUserId}/mute`,
    method: 'post',
    params: { mute }
  })
}

/**
 * 更新我的设置（置顶/免打扰）
 */
export function updateChatMemberSettings(roomId: string | number, isPinned?: boolean, isMuted?: boolean): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/members/room/${roomId}/settings`,
    method: 'put',
    params: { isPinned, isMuted }
  })
}

/**
 * 清空未读消息
 */
export function clearChatUnread(roomId: string | number): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/members/room/${roomId}/read`,
    method: 'post'
  })
}

// ========== 邀请 API ==========

/**
 * 邀请成员
 */
export function inviteChatMembers(data: InviteMembersParams): Promise<ApiResponse<void>> {
  return request({
    url: '/chat/invitations',
    method: 'post',
    data
  })
}

/**
 * 邀请课程所有学员
 */
export function inviteAllCourseStudents(roomId: string | number): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/invitations/room/${roomId}/invite-all`,
    method: 'post'
  })
}

/**
 * 获取待处理的邀请列表
 */
export function getPendingInvitations(): Promise<ApiResponse<ChatInvitation[]>> {
  return request({
    url: '/chat/invitations/pending',
    method: 'get'
  })
}

/**
 * 获取我的所有邀请记录
 */
export function getMyInvitations(): Promise<ApiResponse<ChatInvitation[]>> {
  return request({
    url: '/chat/invitations/my',
    method: 'get'
  })
}

/**
 * 获取待处理邀请数量
 */
export function getPendingInvitationCount(): Promise<ApiResponse<number>> {
  return request({
    url: '/chat/invitations/pending/count',
    method: 'get'
  })
}

/**
 * 接受邀请
 */
export function acceptInvitation(invitationId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/invitations/${invitationId}/accept`,
    method: 'post'
  })
}

/**
 * 拒绝邀请
 */
export function rejectInvitation(invitationId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/invitations/${invitationId}/reject`,
    method: 'post'
  })
}

// ========== 消息 API ==========

/**
 * 发送消息
 */
export function sendChatMessage(data: SendMessageParams): Promise<ApiResponse<ChatMessage>> {
  return request({
    url: '/chat/messages',
    method: 'post',
    data
  })
}

/**
 * 获取聊天室消息（分页）
 */
export function getChatMessages(roomId: string | number, page: number = 1, size: number = 50): Promise<ApiResponse<ChatMessage[]>> {
  return request({
    url: `/chat/messages/room/${roomId}`,
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取历史消息（某消息之前）
 */
export function getChatMessagesBefore(roomId: string | number, beforeId: string | number, limit: number = 20): Promise<ApiResponse<ChatMessage[]>> {
  return request({
    url: `/chat/messages/room/${roomId}/before/${beforeId}`,
    method: 'get',
    params: { limit }
  })
}

/**
 * 撤回消息
 */
export function recallChatMessage(messageId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/chat/messages/${messageId}/recall`,
    method: 'post'
  })
}

// ========== WebSocket ==========

/**
 * 创建WebSocket连接
 */
export function createChatWebSocket(roomId: string | number, userId: string | number): WebSocket {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  // 开发环境直接连接chat-service（注意context-path是/api/chat）
  const wsUrl = import.meta.env.DEV 
    ? `ws://localhost:9005/api/chat/ws/chat?roomId=${roomId}&userId=${userId}`
    : `${protocol}//${host}/ws/chat?roomId=${roomId}&userId=${userId}`
  return new WebSocket(wsUrl)
}

