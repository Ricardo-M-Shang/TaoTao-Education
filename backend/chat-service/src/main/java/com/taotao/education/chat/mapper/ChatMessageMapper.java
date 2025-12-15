package com.taotao.education.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 获取聊天室消息（分页，按时间倒序）
     */
    @Select("""
        SELECT * FROM t_chat_message
        WHERE room_id = #{roomId} AND status = 1 AND deleted = 0
        ORDER BY create_time DESC
        LIMIT #{offset}, #{limit}
        """)
    List<ChatMessage> getRoomMessages(@Param("roomId") Long roomId, 
                                       @Param("offset") int offset, 
                                       @Param("limit") int limit);

    /**
     * 获取某时间之前的消息
     */
    @Select("""
        SELECT * FROM t_chat_message
        WHERE room_id = #{roomId} AND id < #{beforeId} AND status = 1 AND deleted = 0
        ORDER BY create_time DESC
        LIMIT #{limit}
        """)
    List<ChatMessage> getMessagesBefore(@Param("roomId") Long roomId, 
                                         @Param("beforeId") Long beforeId, 
                                         @Param("limit") int limit);

    /**
     * 获取聊天室最新消息
     */
    @Select("""
        SELECT * FROM t_chat_message
        WHERE room_id = #{roomId} AND status = 1 AND deleted = 0
        ORDER BY create_time DESC
        LIMIT 1
        """)
    ChatMessage getLatestMessage(@Param("roomId") Long roomId);
}

