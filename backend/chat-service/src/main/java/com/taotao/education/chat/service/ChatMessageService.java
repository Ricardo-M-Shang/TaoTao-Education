package com.taotao.education.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.chat.dto.SendMessageDTO;
import com.taotao.education.chat.entity.ChatMessage;
import com.taotao.education.chat.vo.ChatMessageVO;

import java.util.List;

/**
 * 聊天消息服务接口
 */
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 发送消息
     */
    ChatMessageVO sendMessage(Long userId, SendMessageDTO dto);

    /**
     * 获取聊天室消息（分页）
     */
    List<ChatMessageVO> getRoomMessages(Long userId, Long roomId, int page, int size);

    /**
     * 获取某消息之前的历史消息
     */
    List<ChatMessageVO> getMessagesBefore(Long userId, Long roomId, Long beforeId, int limit);

    /**
     * 撤回消息
     */
    void recallMessage(Long userId, Long messageId);

    /**
     * 发送系统消息
     */
    ChatMessageVO sendSystemMessage(Long roomId, String content);
}

