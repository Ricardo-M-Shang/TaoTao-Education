package com.taotao.education.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 服务调用配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "service")
public class ServiceConfig {

    // 已迁移至 OpenFeign，不再需要手动配置 URL
    // private String courseUrl = "http://localhost:8082";
    // private String learningUrl = "http://localhost:9004";
}
