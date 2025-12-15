package com.taotao.education.chat.mq;

import com.taotao.education.chat.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 聊天消息生产者
 * 将消息发送到 RabbitMQ 队列进行异步处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息事件（用于持久化和广播）
     */
    public void sendMessageEvent(ChatMessageEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CHAT_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_MESSAGE,
                    event
            );
            log.debug("发送消息事件到队列: messageId={}, roomId={}", 
                    event.getMessageId(), event.getRoomId());
        } catch (Exception e) {
            log.error("发送消息事件失败", e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    /**
     * 发送通知事件（@提及、邀请等）
     */
    public void sendNotificationEvent(ChatMessageEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CHAT_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_NOTIFICATION,
                    event
            );
            log.debug("发送通知事件到队列: roomId={}, type={}", 
                    event.getRoomId(), event.getEventType());
        } catch (Exception e) {
            log.error("发送通知事件失败", e);
        }
    }

    /**
     * 发送统计事件（更新未读数、活跃时间等）
     */
    public void sendStatsEvent(ChatMessageEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CHAT_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_STATS,
                    event
            );
            log.debug("发送统计事件到队列: roomId={}", event.getRoomId());
        } catch (Exception e) {
            log.error("发送统计事件失败", e);
        }
    }
}

