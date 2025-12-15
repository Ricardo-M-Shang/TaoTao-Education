package com.taotao.education.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 聊天服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.taotao.education")
@MapperScan("com.taotao.education.chat.mapper")
@EnableAsync
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}

