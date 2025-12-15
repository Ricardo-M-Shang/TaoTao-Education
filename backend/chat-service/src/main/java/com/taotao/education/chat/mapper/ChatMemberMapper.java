package com.taotao.education.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.chat.entity.ChatMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天室成员Mapper
 */
@Mapper
public interface ChatMemberMapper extends BaseMapper<ChatMember> {

    /**
     * 获取聊天室成员列表
     */
    @Select("""
        SELECT * FROM t_chat_member
        WHERE room_id = #{roomId} AND status = 1 AND deleted = 0
        ORDER BY role DESC, join_time ASC
        """)
    List<ChatMember> getRoomMembers(@Param("roomId") Long roomId);

    /**
     * 获取用户在某聊天室的成员信息
     */
    @Select("""
        SELECT * FROM t_chat_member
        WHERE room_id = #{roomId} AND user_id = #{userId} AND deleted = 0
        """)
    ChatMember getMember(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 更新最后活跃时间
     */
    @Update("""
        UPDATE t_chat_member SET last_active_time = NOW()
        WHERE room_id = #{roomId} AND user_id = #{userId}
        """)
    void updateLastActiveTime(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 清空未读消息数
     */
    @Update("""
        UPDATE t_chat_member SET unread_count = 0
        WHERE room_id = #{roomId} AND user_id = #{userId}
        """)
    void clearUnreadCount(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 增加未读消息数（排除发送者）
     */
    @Update("""
        UPDATE t_chat_member SET unread_count = unread_count + 1
        WHERE room_id = #{roomId} AND user_id != #{senderId} AND status = 1
        """)
    void incrementUnreadCount(@Param("roomId") Long roomId, @Param("senderId") Long senderId);
}

