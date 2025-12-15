package com.taotao.education.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.chat.dto.InviteMembersDTO;
import com.taotao.education.chat.entity.ChatInvitation;
import com.taotao.education.chat.vo.ChatInvitationVO;

import java.util.List;

/**
 * 聊天室邀请服务接口
 */
public interface ChatInvitationService extends IService<ChatInvitation> {

    /**
     * 批量邀请成员
     */
    void inviteMembers(Long inviterId, InviteMembersDTO dto);

    /**
     * 邀请课程所有学员
     */
    void inviteAllCourseStudents(Long inviterId, Long roomId);

    /**
     * 获取待处理的邀请列表
     */
    List<ChatInvitationVO> getPendingInvitations(Long userId);

    /**
     * 获取用户所有邀请记录
     */
    List<ChatInvitationVO> getUserInvitations(Long userId);

    /**
     * 接受邀请
     */
    void acceptInvitation(Long userId, Long invitationId);

    /**
     * 拒绝邀请
     */
    void rejectInvitation(Long userId, Long invitationId);

    /**
     * 获取待处理邀请数量
     */
    int getPendingCount(Long userId);
}

