package com.taotao.education.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.chat.entity.ChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室Mapper
 */
@Mapper
public interface ChatRoomMapper extends BaseMapper<ChatRoom> {

    /**
     * 获取用户加入的聊天室列表
     */
    @Select("""
        SELECT r.* FROM t_chat_room r
        INNER JOIN t_chat_member m ON r.id = m.room_id
        WHERE m.user_id = #{userId} AND m.status = 1 AND r.deleted = 0
        ORDER BY m.is_pinned DESC, m.last_active_time DESC
        """)
    List<ChatRoom> getUserRooms(@Param("userId") Long userId);

    /**
     * 获取讲师创建的聊天室列表
     */
    @Select("""
        SELECT * FROM t_chat_room
        WHERE creator_id = #{creatorId} AND deleted = 0
        ORDER BY create_time DESC
        """)
    List<ChatRoom> getCreatorRooms(@Param("creatorId") Long creatorId);

    /**
     * 根据课程ID获取聊天室
     */
    @Select("SELECT * FROM t_chat_room WHERE course_id = #{courseId} AND deleted = 0")
    ChatRoom getByCourseId(@Param("courseId") Long courseId);
}

