# Nacos 接入与配置指南

本文档详细说明了本项目（TaoTao Education）如何接入 Nacos 注册中心和配置中心，以及如何在 Nacos 中新建和管理配置文件。

## 1. 已接入服务列表

目前以下微服务已完成 Nacos 依赖引入和基础配置：

| 服务名称 | 端口 | 说明 |
| :--- | :--- | :--- |
| `gateway-service` | 9000 | API 网关 |
| `user-service` | 8081 | 用户服务 |
| `course-service` | 8082 | 课程服务 |
| `order-service` | 8083 | 订单服务 |
| `learning-service` | 9004 | 学习服务 |
| `chat-service` | 9005 | 聊天服务 |
| `ai-service` | 9006 | AI 服务 |

所有服务默认配置连接本地 Nacos：
*   **地址**: `127.0.0.1:8848`
*   **账号/密码**: `nacos` / `nacos`
*   **命名空间**: `public` (默认)
*   **分组**: `DEFAULT_GROUP` (默认)

## 2. Nacos 配置迁移步骤

为了实现配置的动态管理，我们需要将本地 `application.yml` 中的**业务配置**迁移到 Nacos。本地只保留连接 Nacos 的引导配置。

### 步骤 1：登录 Nacos 控制台

1.  确保 Nacos Server 已启动。
2.  访问 [http://127.0.0.1:8848/nacos](http://127.0.0.1:8848/nacos)。
3.  输入账号密码（默认均为 `nacos`）。

### 步骤 2：创建配置

1.  在左侧菜单栏选择 **配置管理 > 配置列表**。
2.  点击右上角的 **+** 号按钮。
3.  填写配置信息（参考下方各服务的详细配置内容）。
    *   **Group**: `DEFAULT_GROUP`
    *   **配置格式**: `YAML`

---

## 3. 各服务详细配置内容

请将以下内容分别复制到 Nacos 中对应的 Data ID。

### 3.1 网关服务 (gateway-service)

*   **Data ID**: `gateway-service.yaml`
*   **配置内容**:

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        # 用户服务路由
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=0
        
        # 课程服务路由
        - id: course-service
          uri: lb://course-service
          predicates:
            - Path=/api/course/**
          filters:
            - StripPrefix=0
        
        # 订单服务路由
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/order/**
          filters:
            - StripPrefix=0
        
        # 学习服务路由
        - id: learning-service
          uri: lb://learning-service
          predicates:
            - Path=/api/learning/**
          filters:
            - StripPrefix=0
        
        # 运营统计（订单/收入）路由
        - id: ops-order-service
          uri: lb://order-service
          predicates:
            - Path=/api/ops/**
          filters:
            - StripPrefix=0
        
        # 聊天服务路由
        - id: chat-service
          uri: lb://chat-service
          predicates:
            - Path=/api/chat/**
          filters:
            - StripPrefix=0
        
        # 聊天服务WebSocket路由
        - id: chat-websocket
          uri: lb:ws://chat-service
          predicates:
            - Path=/ws/chat/**
        
        # AI服务路由
        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/ai/**
          filters:
            - StripPrefix=0
      
      # 全局超时配置
      httpclient:
        connect-timeout: 5000
        response-timeout: 30000
      
      # 跨域配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true

  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

# 日志配置
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.taotao.education.gateway: DEBUG
```

### 3.2 用户服务 (user-service)

*   **Data ID**: `user-service.yaml`
*   **配置内容**:

```yaml
spring:
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/taotao_education?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 560868042a
  
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath:/mapper/**/*.xml
  type-aliases-package: com.taotao.education.user.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Knife4j配置
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs

knife4j:
  enable: true
  setting:
    language: zh_cn

# 自定义文件上传路径配置
file:
  upload-path: uploads
  access-url: http://localhost:8081/uploads

# 日志配置
logging:
  level:
    com.taotao.education: DEBUG
```

### 3.3 课程服务 (course-service)

*   **Data ID**: `course-service.yaml`
*   **配置内容**:

```yaml
spring:
  jackson:
    generator:
      write-numbers-as-strings: true

  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/taotao_education?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 560868042a
  
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath:/mapper/**/*.xml
  type-aliases-package: com.taotao.education.course.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Knife4j配置
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs

knife4j:
  enable: true
  setting:
    language: zh_cn

# 日志配置
logging:
  level:
    com.taotao.education: DEBUG

file:
  upload-path: uploads
  access-url: http://localhost:8082/uploads
```

### 3.4 订单服务 (order-service)

*   **Data ID**: `order-service.yaml`
*   **配置内容**:

```yaml
spring:
  # 统一将Long序列化为字符串
  jackson:
    generator:
      write-numbers-as-strings: true
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/taotao_education?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 560868042a
  
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath:/mapper/**/*.xml
  type-aliases-package: com.taotao.education.order.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Knife4j配置
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs

knife4j:
  enable: true
  setting:
    language: zh_cn

# 日志配置
logging:
  level:
    com.taotao.education: DEBUG
```

### 3.5 学习服务 (learning-service)

*   **Data ID**: `learning-service.yaml`
*   **配置内容**:

```yaml
spring:
  # 数据库配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/taotao_education?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: 560868042a
    
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      jedis:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 0
      timeout: 3000ms

# MyBatis Plus配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Knife4j配置
knife4j:
  enable: true
  openapi:
    title: 学习服务API
    description: TaoTao教育平台 - 学习服务API文档
    version: 1.0.0
    concat: shang@taotao.com

# 日志配置
logging:
  level:
    com.taotao.education.learning: debug
    root: info
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 3.6 聊天服务 (chat-service)

*   **Data ID**: `chat-service.yaml`
*   **配置内容**:

```yaml
spring:
  jackson:
    generator:
      write-numbers-as-strings: true
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/taotao_education?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 560868042a
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 10000
  # RabbitMQ 配置
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    # 发送确认
    publisher-confirm-type: correlated
    publisher-returns: true
    # 消费配置
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 10
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
          max-interval: 10000
          multiplier: 2

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

knife4j:
  enable: true
  setting:
    language: zh_cn

logging:
  level:
    com.taotao.education: debug
```

### 3.7 AI服务 (ai-service)

*   **Data ID**: `ai-service.yaml`
*   **配置内容**:

```yaml
spring:
  jackson:
    generator:
      write-numbers-as-strings: true

  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

# DeepSeek AI 配置
deepseek:
  api-key: ${DEEPSEEK_API_KEY:sk-e84f9f6514ed4ebbb2db70dae7d87004}
  base-url: https://api.deepseek.com
  model: deepseek-chat
  max-tokens: 2000
  temperature: 0.7

# 内部服务调用地址
service:
  course-url: http://localhost:8082
  learning-url: http://localhost:9004

# 推荐配置
recommend:
  cache-ttl-minutes: 30
  default-count: 5
  max-count: 10

# Knife4j配置
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs

knife4j:
  enable: true
  setting:
    language: zh_cn

# 日志配置
logging:
  level:
    com.taotao.education: DEBUG
```

## 4. 本地配置说明

迁移后，本地 `src/main/resources/application.yml` 仅保留必要的引导配置。以 `user-service` 为例：

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yaml
        username: nacos
        password: nacos
  config:
    import:
      # optional 表示如果配置中心没有该文件，也不报错（使用本地默认或空配置启动）
      - optional:nacos:user-service.yaml
```

**注意**：`spring.config.import` 必须配置，否则无法加载远程配置。
