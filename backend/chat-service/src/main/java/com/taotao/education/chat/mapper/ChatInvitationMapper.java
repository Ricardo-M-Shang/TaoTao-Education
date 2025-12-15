package com.taotao.education.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.chat.entity.ChatInvitation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室邀请Mapper
 */
@Mapper
public interface ChatInvitationMapper extends BaseMapper<ChatInvitation> {

    /**
     * 获取用户收到的邀请列表
     */
    @Select("""
        SELECT * FROM t_chat_invitation
        WHERE invitee_id = #{inviteeId} AND status = 0 AND expire_time > NOW() AND deleted = 0
        ORDER BY create_time DESC
        """)
    List<ChatInvitation> getPendingInvitations(@Param("inviteeId") Long inviteeId);

    /**
     * 获取用户的所有邀请记录
     */
    @Select("""
        SELECT * FROM t_chat_invitation
        WHERE invitee_id = #{inviteeId} AND deleted = 0
        ORDER BY create_time DESC
        """)
    List<ChatInvitation> getUserInvitations(@Param("inviteeId") Long inviteeId);

    /**
     * 检查是否已发送邀请
     */
    @Select("""
        SELECT * FROM t_chat_invitation
        WHERE room_id = #{roomId} AND invitee_id = #{inviteeId} AND status = 0 AND deleted = 0
        """)
    ChatInvitation checkExistingInvitation(@Param("roomId") Long roomId, @Param("inviteeId") Long inviteeId);
}

