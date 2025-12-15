package com.taotao.education.chat.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.education.chat.config.RedisPubSubConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Redis 消息发布者
 * 将消息广播到所有实例
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.instance-id:}")
    private String configuredInstanceId;

    private String instanceId;

    /**
     * 获取实例ID（懒加载，因为 @Value 可能在初始化时还没准备好）
     */
    private String getInstanceId() {
        if (instanceId == null) {
            instanceId = configuredInstanceId != null && !configuredInstanceId.isEmpty() 
                    ? configuredInstanceId 
                    : UUID.randomUUID().toString();
        }
        return instanceId;
    }

    /**
     * 发布消息到 Redis 频道
     */
    public void publish(Long roomId, String type, Object data) {
        try {
            String dataJson = objectMapper.writeValueAsString(data);
            
            RedisBroadcastMessage message = RedisBroadcastMessage.builder()
                    .roomId(roomId)
                    .type(type)
                    .data(dataJson)
                    .instanceId(getInstanceId())
                    .build();

            String channel = RedisPubSubConfig.CHAT_CHANNEL_PREFIX + roomId;
            // 直接发送对象，让 RedisTemplate 的 serializer 处理序列化
            redisTemplate.convertAndSend(channel, message);
            
            log.debug("发布消息到 Redis: channel={}, type={}", channel, type);
        } catch (Exception e) {
            log.error("发布 Redis 消息失败", e);
        }
    }

    /**
     * 发布聊天消息
     */
    public void publishChatMessage(Long roomId, Object messageVO) {
        publish(roomId, "message", messageVO);
    }

    /**
     * 发布用户状态变更
     */
    public void publishUserStatus(Long roomId, Long userId, boolean online) {
        publish(roomId, "userStatus", new UserStatusData(userId, online));
    }

    /**
     * 用户状态数据
     */
    private record UserStatusData(Long userId, boolean online) {}
}

