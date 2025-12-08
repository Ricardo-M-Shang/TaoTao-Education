package com.taotao.education.course;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 课程服务启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.taotao.education.course", "com.taotao.education.common"})
@MapperScan("com.taotao.education.course.mapper")
public class CourseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApplication.class, args);
    }
}

