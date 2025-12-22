package com.taotao.education.order.mq;

import com.rabbitmq.client.Channel;
import com.taotao.education.order.config.RabbitMQConfig;
import com.taotao.education.order.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillConsumer {

    private final CouponService couponService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void onMessage(Map<String, Object> msg, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            Long userId = Long.valueOf(msg.get("userId").toString());
            Long couponId = Long.valueOf(msg.get("couponId").toString());
            
            log.info("Processing seckill: userId={}, couponId={}", userId, couponId);
            couponService.receiveSeckill(userId, couponId);
            
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Seckill processing failed", e);
            // Nack and discard to prevent loop (real world: use DLQ)
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
