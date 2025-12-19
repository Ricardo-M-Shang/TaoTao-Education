package com.taotao.education.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 推荐配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "recommend")
public class RecommendConfig {

    /**
     * 缓存过期时间（分钟）
     */
    private Integer cacheTtlMinutes = 30;

    /**
     * 默认推荐数量
     */
    private Integer defaultCount = 5;

    /**
     * 最大推荐数量
     */
    private Integer maxCount = 10;
}

