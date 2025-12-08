# TaoTao在线教育平台

## 项目简介

TaoTao在线教育平台是一个全栈在线教育系统，包含用户管理、课程管理、订单支付等核心功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2 + Spring Cloud Gateway
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: MyBatis Plus
- **API文档**: Knife4j (Swagger)
- **认证**: JWT

### 前端
- **框架**: Vue 3 + TypeScript
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **构建工具**: Vite

## 项目结构

```
TaoTao-Education/
├── backend/                    # 后端代码
│   ├── common/                 # 公共模块
│   ├── gateway/                # API网关
│   ├── user-service/           # 用户服务 (端口: 8081)
│   ├── course-service/         # 课程服务 (端口: 8082)
│   ├── order-service/          # 订单服务 (端口: 8083)
│   ├── sql/                    # 数据库脚本
│   └── pom.xml                 # 父POM
├── frontend/                   # 前端代码
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── layouts/           # 布局组件
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # 状态管理
│   │   ├── types/             # TypeScript类型
│   │   ├── utils/             # 工具函数
│   │   └── views/             # 页面组件
│   └── package.json
└── 项目设计方案.md
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 1. 初始化数据库

```bash
# 进入后端目录
cd backend

# 执行SQL脚本创建数据库和表
mysql -u root -p < sql/init.sql
```

### 2. 启动后端服务

```bash
# 进入后端目录
cd backend

# 安装依赖
mvn clean install -DskipTests

# 启动网关服务 (端口: 9000)
cd gateway && mvn spring-boot:run

# 新开终端，启动用户服务 (端口: 8081)
cd user-service && mvn spring-boot:run

# 新开终端，启动课程服务 (端口: 8082)
cd course-service && mvn spring-boot:run

# 新开终端，启动订单服务 (端口: 8083)
cd order-service && mvn spring-boot:run
```

### 3. 启动前端

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 4. 访问应用

- 前端页面: http://localhost:3000
- API网关: http://localhost:9000
- API文档: http://localhost:8081/doc.html (用户服务)

### 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 管理员 |
| teacher | 123456 | 讲师 |
| student | 123456 | 学员 |

## 核心功能

### 用户模块
- 用户注册、登录
- JWT Token认证
- 个人信息管理
- 密码修改

### 课程模块
- 课程分类管理
- 课程列表、详情
- 章节、课时管理
- 课程搜索、筛选

### 订单模块
- 创建订单
- 订单支付（模拟）
- 订单管理
- 用户课程关联

## API接口

### 用户接口 (/api/user)
- POST /register - 用户注册
- POST /login - 用户登录
- GET /info - 获取用户信息
- PUT /info - 更新用户信息
- POST /password/change - 修改密码
- POST /logout - 退出登录

### 课程接口 (/api/course)
- GET /list - 课程列表
- GET /detail/{id} - 课程详情
- GET /category/tree - 分类树
- POST /create - 创建课程
- PUT /update/{id} - 更新课程
- POST /publish/{id} - 发布课程

### 订单接口 (/api/order)
- POST /create - 创建订单
- GET /detail/{orderNo} - 订单详情
- GET /list - 用户订单列表
- POST /cancel/{orderNo} - 取消订单
- POST /pay - 支付订单
- GET /check/{courseId} - 检查是否已购买

## 配置说明

### 数据库配置
在各服务的 `application.yml` 中配置数据库连接：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/taotao_education
    username: root
    password: 560868042a
```

### Redis配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 后续扩展

根据项目设计方案，可继续添加以下功能：
- 直播服务
- 视频服务（上传、转码）
- AI智能推荐
- 在线考试系统
- 社交学习社区
- 营销系统（优惠券、分销）

