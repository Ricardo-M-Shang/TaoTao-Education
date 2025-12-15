package com.taotao.education.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.chat.entity.ChatMember;
import com.taotao.education.chat.vo.ChatMemberVO;

import java.util.List;

/**
 * 聊天室成员服务接口
 */
public interface ChatMemberService extends IService<ChatMember> {

    /**
     * 加入聊天室
     */
    void joinRoom(Long userId, Long roomId, String nickname, String avatar, Integer role);

    /**
     * 退出聊天室
     */
    void leaveRoom(Long userId, Long roomId);

    /**
     * 获取聊天室成员列表
     */
    List<ChatMemberVO> getRoomMembers(Long roomId);

    /**
     * 检查用户是否是聊天室成员
     */
    boolean isMember(Long userId, Long roomId);

    /**
     * 获取成员信息
     */
    ChatMember getMember(Long userId, Long roomId);

    /**
     * 移除成员（仅管理员/创建者可操作）
     */
    void removeMember(Long operatorId, Long roomId, Long targetUserId);

    /**
     * 设置管理员
     */
    void setAdmin(Long operatorId, Long roomId, Long targetUserId, boolean isAdmin);

    /**
     * 禁言成员
     */
    void muteMember(Long operatorId, Long roomId, Long targetUserId, boolean mute);

    /**
     * 更新成员设置（置顶/免打扰）
     */
    void updateMemberSettings(Long userId, Long roomId, Boolean isPinned, Boolean isMuted);

    /**
     * 清空未读消息数
     */
    void clearUnreadCount(Long userId, Long roomId);
}

