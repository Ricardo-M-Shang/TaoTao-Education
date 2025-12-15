package com.taotao.education.chat.config;

import com.taotao.education.chat.mq.RedisChatMessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

/**
 * Redis Pub/Sub 配置
 * 用于多实例间的 WebSocket 消息广播
 * 注意：RedisTemplate 由 common 模块的 RedisConfig 提供
 */
@Configuration
public class RedisPubSubConfig {

    // Redis 频道名称
    public static final String CHAT_CHANNEL_PATTERN = "chat:broadcast:*";
    public static final String CHAT_CHANNEL_PREFIX = "chat:broadcast:";

    /**
     * Redis 消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 订阅 chat:broadcast:* 频道
        container.addMessageListener(messageListenerAdapter, new PatternTopic(CHAT_CHANNEL_PATTERN));
        return container;
    }

    /**
     * 消息监听适配器
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisChatMessageSubscriber subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onMessage");
        adapter.setSerializer(new GenericJackson2JsonRedisSerializer());
        return adapter;
    }
}

