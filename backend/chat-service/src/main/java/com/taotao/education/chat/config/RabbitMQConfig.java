package com.taotao.education.chat.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMQConfig {

    // ============ 交换机名称 ============
    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String CHAT_DEAD_LETTER_EXCHANGE = "chat.dlx.exchange";

    // ============ 队列名称 ============
    public static final String CHAT_MESSAGE_QUEUE = "chat.message.queue";
    public static final String CHAT_NOTIFICATION_QUEUE = "chat.notification.queue";
    public static final String CHAT_STATS_QUEUE = "chat.stats.queue";
    public static final String CHAT_DEAD_LETTER_QUEUE = "chat.dlx.queue";

    // ============ 路由键 ============
    public static final String ROUTING_KEY_MESSAGE = "chat.message";
    public static final String ROUTING_KEY_NOTIFICATION = "chat.notification";
    public static final String ROUTING_KEY_STATS = "chat.stats";
    public static final String ROUTING_KEY_DLX = "chat.dlx";

    /**
     * JSON 消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        
        // 消息发送确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("消息发送失败: " + cause);
            }
        });
        
        // 消息返回回调（路由失败时）
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息路由失败: " + returned.getMessage());
        });
        
        return rabbitTemplate;
    }

    /**
     * 监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        return factory;
    }

    // ============ 交换机声明 ============

    /**
     * 聊天主交换机（Topic类型）
     */
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE, true, false);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(CHAT_DEAD_LETTER_EXCHANGE, true, false);
    }

    // ============ 队列声明 ============

    /**
     * 聊天消息队列（带死信配置）
     */
    @Bean
    public Queue chatMessageQueue() {
        return QueueBuilder.durable(CHAT_MESSAGE_QUEUE)
                .withArgument("x-dead-letter-exchange", CHAT_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLX)
                .build();
    }

    /**
     * 通知队列
     */
    @Bean
    public Queue chatNotificationQueue() {
        return QueueBuilder.durable(CHAT_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", CHAT_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLX)
                .build();
    }

    /**
     * 统计队列
     */
    @Bean
    public Queue chatStatsQueue() {
        return QueueBuilder.durable(CHAT_STATS_QUEUE)
                .withArgument("x-dead-letter-exchange", CHAT_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLX)
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(CHAT_DEAD_LETTER_QUEUE).build();
    }

    // ============ 绑定关系 ============

    @Bean
    public Binding messageBinding() {
        return BindingBuilder.bind(chatMessageQueue())
                .to(chatExchange())
                .with(ROUTING_KEY_MESSAGE);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(chatNotificationQueue())
                .to(chatExchange())
                .with(ROUTING_KEY_NOTIFICATION);
    }

    @Bean
    public Binding statsBinding() {
        return BindingBuilder.bind(chatStatsQueue())
                .to(chatExchange())
                .with(ROUTING_KEY_STATS);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(ROUTING_KEY_DLX);
    }
}

