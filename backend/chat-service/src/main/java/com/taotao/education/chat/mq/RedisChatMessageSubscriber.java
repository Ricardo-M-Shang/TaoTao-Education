package com.taotao.education.chat.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.education.chat.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis 消息订阅者
 * 接收其他实例广播的消息，并推送到本地 WebSocket
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessageSubscriber implements MessageListener {

    private final ChatWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.instance-id:${random.uuid}}")
    private String instanceId;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            
            RedisBroadcastMessage broadcastMessage;
            
            if (body.startsWith("[")) {
                // GenericJackson2JsonRedisSerializer 格式: ["className", {data}]
                // 解析数组，取第二个元素作为实际数据
                Object[] array = objectMapper.readValue(body, Object[].class);
                if (array.length >= 2) {
                    // 第二个元素是实际的对象数据（已经是 LinkedHashMap）
                    broadcastMessage = objectMapper.convertValue(array[1], RedisBroadcastMessage.class);
                } else {
                    log.warn("无效的 Redis 消息格式: {}", body);
                    return;
                }
            } else if (body.startsWith("\"")) {
                // 双重序列化的字符串
                String unescaped = objectMapper.readValue(body, String.class);
                broadcastMessage = objectMapper.readValue(unescaped, RedisBroadcastMessage.class);
            } else {
                // 普通 JSON 对象格式
                broadcastMessage = objectMapper.readValue(body, RedisBroadcastMessage.class);
            }

            // 忽略自己发送的广播
            if (instanceId.equals(broadcastMessage.getInstanceId())) {
                return;
            }

            log.debug("收到 Redis 广播消息: roomId={}, type={}", 
                    broadcastMessage.getRoomId(), broadcastMessage.getType());

            // 转发到本地 WebSocket
            webSocketHandler.broadcastFromRedis(
                    broadcastMessage.getRoomId(),
                    broadcastMessage.getType(),
                    broadcastMessage.getData()
            );

        } catch (Exception e) {
            log.error("处理 Redis 广播消息失败", e);
        }
    }
}

