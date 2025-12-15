package com.taotao.education.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.chat.dto.SendMessageDTO;
import com.taotao.education.chat.entity.ChatMember;
import com.taotao.education.chat.entity.ChatMessage;
import com.taotao.education.chat.mapper.ChatMemberMapper;
import com.taotao.education.chat.mapper.ChatMessageMapper;
import com.taotao.education.chat.mq.ChatMessageEvent;
import com.taotao.education.chat.mq.ChatMessageProducer;
import com.taotao.education.chat.service.ChatMemberService;
import com.taotao.education.chat.service.ChatMessageService;
import com.taotao.education.chat.vo.ChatMessageVO;
import com.taotao.education.chat.websocket.ChatWebSocketHandler;
import com.taotao.education.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天消息服务实现
 * 使用 RabbitMQ 进行异步消息处理，支持高并发和可靠投递
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    private final ChatMemberMapper memberMapper;
    private final ChatMemberService memberService;
    private final ChatWebSocketHandler webSocketHandler;
    private final ChatMessageProducer messageProducer;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendMessage(Long userId, SendMessageDTO dto) {
        // 检查是否是成员
        ChatMember member = memberMapper.getMember(dto.getRoomId(), userId);
        if (member == null || member.getStatus() != 1) {
            throw new BusinessException("您不是聊天室成员或已被禁言");
        }
        if (member.getStatus() == 2) {
            throw new BusinessException("您已被禁言");
        }

        // 获取用户信息
        String userSql = "SELECT nickname, avatar, role FROM t_user WHERE id = ? AND deleted = 0";
        List<Map<String, Object>> userInfo = jdbcTemplate.queryForList(userSql, userId);
        if (userInfo.isEmpty()) {
            throw new BusinessException("用户不存在");
        }
        Map<String, Object> user = userInfo.get(0);

        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setRoomId(dto.getRoomId());
        message.setSenderId(userId);
        message.setSenderName((String) user.get("nickname"));
        message.setSenderAvatar((String) user.get("avatar"));
        message.setSenderRole((Integer) user.get("role"));
        message.setMessageType(dto.getMessageType());
        message.setContent(dto.getContent());
        message.setAttachmentUrl(dto.getAttachmentUrl());
        message.setAttachmentName(dto.getAttachmentName());
        message.setAttachmentSize(dto.getAttachmentSize());
        message.setReplyToId(dto.getReplyToId());
        message.setStatus(1);
        message.setMentionedUserIds(dto.getMentionedUserIds());

        this.save(message);

        // 转换为VO（返回给发送者，isMine=true）
        ChatMessageVO vo = convertToVO(message, userId);

        // 构建消息事件
        ChatMessageEvent event = buildMessageEvent(message, vo);

        // 异步发送消息事件到 RabbitMQ
        try {
            // 1. 广播消息到 WebSocket（通过消息队列）
            messageProducer.sendMessageEvent(event);
            
            // 2. 异步更新统计数据
            messageProducer.sendStatsEvent(event);
            
            // 3. 如果有@提及，发送通知
            if (dto.getMentionedUserIds() != null && !dto.getMentionedUserIds().isEmpty()) {
                messageProducer.sendNotificationEvent(event);
            }
            
            log.debug("消息已发送到队列: messageId={}", message.getId());
        } catch (Exception e) {
            log.error("发送消息到队列失败，使用同步方式处理", e);
            // 降级：如果队列不可用，使用同步方式
            fallbackSyncBroadcast(dto.getRoomId(), userId, vo);
        }

        return vo;
    }

    @Override
    public List<ChatMessageVO> getRoomMessages(Long userId, Long roomId, int page, int size) {
        // 检查是否是成员
        if (!memberService.isMember(userId, roomId)) {
            throw new BusinessException("您不是聊天室成员");
        }

        int offset = (page - 1) * size;
        List<ChatMessage> messages = baseMapper.getRoomMessages(roomId, offset, size);
        
        // 倒序获取后需要反转为正序显示
        Collections.reverse(messages);
        
        return messages.stream()
            .map(msg -> convertToVO(msg, userId))
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVO> getMessagesBefore(Long userId, Long roomId, Long beforeId, int limit) {
        // 检查是否是成员
        if (!memberService.isMember(userId, roomId)) {
            throw new BusinessException("您不是聊天室成员");
        }

        List<ChatMessage> messages = baseMapper.getMessagesBefore(roomId, beforeId, limit);
        
        // 倒序获取后需要反转为正序显示
        Collections.reverse(messages);
        
        return messages.stream()
            .map(msg -> convertToVO(msg, userId))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long userId, Long messageId) {
        ChatMessage message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getSenderId().equals(userId)) {
            // 检查是否是管理员/创建者
            ChatMember member = memberMapper.getMember(message.getRoomId(), userId);
            if (member == null || member.getRole() < 2) {
                throw new BusinessException("您没有权限撤回此消息");
            }
        }

        // 检查时间（2分钟内可撤回）
        if (message.getSenderId().equals(userId) && 
            message.getCreateTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
            throw new BusinessException("超过2分钟的消息不能撤回");
        }

        message.setStatus(0);
        message.setContent("[消息已撤回]");
        this.updateById(message);

        // 通知其他用户消息已撤回
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(messageId);
        vo.setRoomId(message.getRoomId());
        vo.setContent("[消息已撤回]");
        vo.setMessageType(4); // 系统消息类型
        webSocketHandler.broadcastMessage(message.getRoomId(), vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendSystemMessage(Long roomId, String content) {
        ChatMessage message = new ChatMessage();
        message.setRoomId(roomId);
        message.setSenderId(0L);
        message.setSenderName("系统消息");
        message.setMessageType(4);
        message.setContent(content);
        message.setStatus(1);

        this.save(message);

        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setIsMine(false);

        // 广播系统消息
        webSocketHandler.broadcastMessage(roomId, vo);

        return vo;
    }

    private ChatMessageVO convertToVO(ChatMessage message, Long currentUserId) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setIsMine(message.getSenderId().equals(currentUserId));

        // 如果有回复的消息，获取回复内容预览
        if (message.getReplyToId() != null) {
            ChatMessage replyTo = this.getById(message.getReplyToId());
            if (replyTo != null) {
                String preview = replyTo.getContent();
                if (preview != null && preview.length() > 50) {
                    preview = preview.substring(0, 50) + "...";
                }
                vo.setReplyToContent(preview);
            }
        }

        return vo;
    }

    /**
     * 构建消息事件
     */
    private ChatMessageEvent buildMessageEvent(ChatMessage message, ChatMessageVO vo) {
        return ChatMessageEvent.builder()
                .eventType(ChatMessageEvent.EventType.NEW_MESSAGE)
                .messageId(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderAvatar(message.getSenderAvatar())
                .senderRole(message.getSenderRole())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentName(message.getAttachmentName())
                .attachmentSize(message.getAttachmentSize())
                .replyToId(message.getReplyToId())
                .replyToContent(vo.getReplyToContent())
                .mentionedUserIds(message.getMentionedUserIds())
                .createTime(message.getCreateTime())
                .build();
    }

    /**
     * 降级处理：同步广播消息（当 RabbitMQ 不可用时）
     */
    private void fallbackSyncBroadcast(Long roomId, Long senderId, ChatMessageVO vo) {
        // 更新发送者活跃时间
        memberMapper.updateLastActiveTime(roomId, senderId);

        // 增加其他成员的未读消息数
        memberMapper.incrementUnreadCount(roomId, senderId);

        // 广播消息
        ChatMessageVO broadcastVo = new ChatMessageVO();
        BeanUtils.copyProperties(vo, broadcastVo);
        broadcastVo.setIsMine(null);
        webSocketHandler.broadcastMessage(roomId, broadcastVo);
    }
}

