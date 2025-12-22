package com.taotao.education.order.service.impl;

import com.taotao.education.common.result.Result;
import com.taotao.education.order.config.RabbitMQConfig;
import com.taotao.education.order.entity.Coupon;
import com.taotao.education.order.service.CouponService;
import com.taotao.education.order.service.SeckillService;
import com.taotao.education.order.vo.CouponVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final CouponService couponService;
    
    private DefaultRedisScript<Long> seckillScript;

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/seckill.lua")));
        seckillScript.setResultType(Long.class);
    }

    @Override
    public Result<String> doSeckill(Long userId, Long couponId) {
        String stockKey = "seckill:stock:" + couponId;
        String userKey = "seckill:user:" + couponId;
        
        // Execute Lua
        Long result = redisTemplate.execute(seckillScript, Arrays.asList(stockKey, userKey), String.valueOf(userId));

        if (result == null) {
            return Result.fail("秒杀失败");
        }
        if (result == -1) {
            return Result.fail("您已经抢过了");
        }
        if (result == -2) {
            return Result.fail("活动未开始或不存在");
        }
        if (result == 0) {
            return Result.fail("手慢了，已抢光");
        }

        // 3. Send to MQ
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId);
        msg.put("couponId", couponId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE, RabbitMQConfig.SECKILL_ROUTING_KEY, msg);

        return Result.success("抢购成功，正在发放...");
    }

    @Override
    public void preheat(Coupon coupon) {
        String stockKey = "seckill:stock:" + coupon.getId();
        redisTemplate.opsForValue().set(stockKey, String.valueOf(coupon.getStock()));
    }

    @Override
    public List<CouponVO> listSeckillCoupons() {
        return couponService.listSeckillCoupons();
    }
}
