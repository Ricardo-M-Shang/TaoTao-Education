package com.taotao.education.chat.mq;

import com.rabbitmq.client.Channel;
import com.taotao.education.chat.config.RabbitMQConfig;
import com.taotao.education.chat.mapper.ChatMemberMapper;
import com.taotao.education.chat.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 聊天消息消费者
 * 处理消息队列中的事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {

    private final RedisChatMessagePublisher redisPublisher;
    private final ChatMemberMapper memberMapper;

    /**
     * 消费消息事件 - 广播到 WebSocket
     */
    @RabbitListener(queues = RabbitMQConfig.CHAT_MESSAGE_QUEUE)
    public void handleMessageEvent(ChatMessageEvent event, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.debug("消费消息事件: messageId={}, roomId={}", event.getMessageId(), event.getRoomId());

            // 构建消息 VO
            ChatMessageVO vo = buildMessageVO(event);

            // 通过 Redis Pub/Sub 广播到所有实例
            redisPublisher.publishChatMessage(event.getRoomId(), vo);

            // 确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("处理消息事件失败", e);
            handleException(channel, deliveryTag, e);
        }
    }

    /**
     * 消费通知事件 - 处理 @提及等通知
     */
    @RabbitListener(queues = RabbitMQConfig.CHAT_NOTIFICATION_QUEUE)
    public void handleNotificationEvent(ChatMessageEvent event, Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.debug("消费通知事件: roomId={}, type={}", event.getRoomId(), event.getEventType());

            // 处理 @提及通知
            if (event.getMentionedUserIds() != null && !event.getMentionedUserIds().isEmpty()) {
                // TODO: 发送站内通知或推送
                log.info("处理@提及通知: mentionedUsers={}", event.getMentionedUserIds());
            }

            // 确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("处理通知事件失败", e);
            handleException(channel, deliveryTag, e);
        }
    }

    /**
     * 消费统计事件 - 更新未读数等
     */
    @RabbitListener(queues = RabbitMQConfig.CHAT_STATS_QUEUE)
    public void handleStatsEvent(ChatMessageEvent event, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.debug("消费统计事件: roomId={}", event.getRoomId());

            // 更新发送者活跃时间
            memberMapper.updateLastActiveTime(event.getRoomId(), event.getSenderId());

            // 增加其他成员的未读消息数
            memberMapper.incrementUnreadCount(event.getRoomId(), event.getSenderId());

            // 确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("处理统计事件失败", e);
            handleException(channel, deliveryTag, e);
        }
    }

    /**
     * 构建消息 VO
     */
    private ChatMessageVO buildMessageVO(ChatMessageEvent event) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(event.getMessageId());
        vo.setRoomId(event.getRoomId());
        vo.setSenderId(event.getSenderId());
        vo.setSenderName(event.getSenderName());
        vo.setSenderAvatar(event.getSenderAvatar());
        vo.setSenderRole(event.getSenderRole());
        vo.setMessageType(event.getMessageType());
        vo.setContent(event.getContent());
        vo.setAttachmentUrl(event.getAttachmentUrl());
        vo.setAttachmentName(event.getAttachmentName());
        vo.setAttachmentSize(event.getAttachmentSize());
        vo.setReplyToId(event.getReplyToId());
        vo.setReplyToContent(event.getReplyToContent());
        vo.setMentionedUserIds(event.getMentionedUserIds());
        vo.setCreateTime(event.getCreateTime());
        vo.setIsMine(null); // 让客户端自己判断
        return vo;
    }

    /**
     * 处理异常
     */
    private void handleException(Channel channel, long deliveryTag, Exception e) {
        try {
            // 拒绝消息并重新入队（最多重试3次后进入死信队列）
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException ex) {
            log.error("拒绝消息失败", ex);
        }
    }
}

