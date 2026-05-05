# Docker 快速启动（本项目）

## 1. 前置条件

- 已安装 Docker Desktop（Windows）
- 确保 3306/6379/5672/8848/9000 等端口未被占用

## 2. 打包后端 Jar

在项目根目录执行：

```bash
cd backend
mvn clean package -DskipTests
```

打包完成后，请确认这些文件存在：

- `backend/gateway/target/gateway-1.0.0.jar`
- `backend/user-service/target/user-service-1.0.0.jar`
- `backend/course-service/target/course-service-1.0.0.jar`
- `backend/order-service/target/order-service-1.0.0.jar`
- `backend/learning-service/target/learning-service-1.0.0.jar`
- `backend/chat-service/target/chat-service-1.0.0.jar`
- `backend/ai-service/target/ai-service-1.0.0.jar`

## 3. 一键启动

回到项目根目录执行：

```bash
docker compose up -d --build
```

查看状态：

```bash
docker compose ps
```

查看某个服务日志（示例）：

```bash
docker compose logs -f gateway-service
```

## 4. 访问入口

- 网关：`http://localhost:9000`
- Nacos：`http://localhost:8848/nacos`
- Sentinel：`http://localhost:8858`
- RabbitMQ 管理台：`http://localhost:15672`

## 5. 停止与清理

停止：

```bash
docker compose down
```

停止并删除数据卷（会清空 MySQL 数据）：

```bash
docker compose down -v
```
