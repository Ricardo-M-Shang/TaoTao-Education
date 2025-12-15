package com.taotao.education.learning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 学习服务启动类
 */
@SpringBootApplication(
    scanBasePackages = {"com.taotao.education.common", "com.taotao.education.learning"},
    excludeName = {
        "org.springframework.cloud.gateway.config.GatewayAutoConfiguration",
        "org.springframework.cloud.gateway.config.GatewayClassPathWarningAutoConfiguration",
        "org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration",
        "org.springframework.cloud.gateway.config.GatewayMetricsAutoConfiguration",
        "org.springframework.cloud.gateway.config.GatewayResilience4JCircuitBreakerAutoConfiguration",
        "org.springframework.cloud.gateway.config.GatewayNoLoadBalancerClientAutoConfiguration",
        "org.springframework.cloud.gateway.config.GatewayLoadBalancerClientAutoConfiguration",
        "org.springframework.cloud.gateway.config.GatewayReactiveLoadBalancerClientAutoConfiguration",
        "org.springframework.cloud.gateway.config.SimpleUrlHandlerMappingGlobalCorsAutoConfiguration"
    }
)
@MapperScan("com.taotao.education.learning.mapper")
@EnableAsync
@EnableScheduling
public class LearningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningServiceApplication.class, args);
        System.out.println("====== 学习服务启动成功 ======");
    }
}
