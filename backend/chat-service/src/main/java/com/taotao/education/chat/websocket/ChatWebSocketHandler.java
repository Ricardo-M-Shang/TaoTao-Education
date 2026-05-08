package com.taotao.education.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.education.chat.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器
 * 支持多实例部署，通过 Redis Pub/Sub 进行消息广播
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // roomId -> (userId -> session)
    private static final Map<Long, Map<Long, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    
    // sessionId -> (roomId, userId)
    private static final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    private static final String ONLINE_USERS_KEY = "chat:online:room:";
    
    // Redis 发布者（延迟注入，避免循环依赖）
    private com.taotao.education.chat.mq.RedisChatMessagePublisher redisPublisher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从URL参数获取roomId和userId
        Map<String, Object> attributes = session.getAttributes();
        Long roomId = (Long) attributes.get("roomId");
        Long userId = (Long) attributes.get("userId");

        if (roomId == null || userId == null) {
            log.warn("WebSocket连接缺少必要参数");
            session.close();
            return;
        }

        // 将session加入房间
        roomSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(userId, session);
        sessionInfoMap.put(session.getId(), new SessionInfo(roomId, userId));

        // 记录在线用户到Redis
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY + roomId, userId.toString());

        log.info("用户 {} 加入聊天室 {}", userId, roomId);

        // 广播用户上线消息
        broadcastUserStatus(roomId, userId, true);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SessionInfo info = sessionInfoMap.remove(session.getId());
        if (info != null) {
            Map<Long, WebSocketSession> sessions = roomSessions.get(info.roomId);
            if (sessions != null) {
                sessions.remove(info.userId);
                if (sessions.isEmpty()) {
                    roomSessions.remove(info.roomId);
                }
            }

            // 从Redis移除在线用户
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY + info.roomId, info.userId.toString());

            log.info("用户 {} 离开聊天室 {}", info.userId, info.roomId);

            // 广播用户下线消息
            broadcastUserStatus(info.roomId, info.userId, false);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理心跳或其他消息
        String payload = message.getPayload();
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
        session.close();
    }

    /**
     * 设置 Redis 发布者（用于解决循环依赖）
     */
    @org.springframework.beans.factory.annotation.Autowired
    public void setRedisPublisher(com.taotao.education.chat.mq.RedisChatMessagePublisher redisPublisher) {
        this.redisPublisher = redisPublisher;
    }

    /**
     * 广播消息到房间（同时通过 Redis 广播到其他实例）
     */
    public void broadcastMessage(Long roomId, ChatMessageVO message) {
        // 先广播到本地 WebSocket
        broadcastToLocalSessions(roomId, "message", message);
        
        // 再通过 Redis Pub/Sub 广播到其他实例
        if (redisPublisher != null) {
            redisPublisher.publishChatMessage(roomId, message);
        }
    }

    /**
     * 仅广播到本地 WebSocket（不通过 Redis）
     * 用于处理从 Redis 接收的广播消息
     */
    public void broadcastToLocalSessions(Long roomId, String type, Object data) {
        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet()) {
                Long sessionUserId = entry.getKey();
                WebSocketSession session = entry.getValue();
                if (session.isOpen()) {
                    try {
                        Object sessionData = enrichDataForSession(type, data, sessionUserId);
                        String messageJson = objectMapper.writeValueAsString(Map.of(
                            "type", type,
                            "data", sessionData
                        ));
                        TextMessage textMessage = new TextMessage(messageJson);
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("发送消息失败", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("序列化消息失败", e);
        }
    }

    /**
     * 从 Redis 接收广播消息并转发到本地 WebSocket
     */
    public void broadcastFromRedis(Long roomId, String type, String dataJson) {
        try {
            Object data = objectMapper.readValue(dataJson, Object.class);
            // 复用本地广播逻辑，按会话动态补充 isMine
            broadcastToLocalSessions(roomId, type, data);
        } catch (Exception e) {
            log.error("处理 Redis 广播消息失败", e);
        }
    }

    /**
     * 广播用户状态变更（通过 Redis 广播到所有实例）
     */
    private void broadcastUserStatus(Long roomId, Long userId, boolean online) {
        // 广播到本地
        broadcastToLocalSessions(roomId, "userStatus", Map.of("userId", userId, "online", online));
        
        // 通过 Redis 广播到其他实例
        if (redisPublisher != null) {
            redisPublisher.publishUserStatus(roomId, userId, online);
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(Long roomId, Long userId, Object message) {
        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) {
            return;
        }

        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String messageJson = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(messageJson));
            } catch (Exception e) {
                log.error("发送消息给用户失败", e);
            }
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long roomId, Long userId) {
        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) {
            return false;
        }
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 按会话补充消息字段（目前仅 message 类型补充 isMine）
     */
    private Object enrichDataForSession(String type, Object data, Long sessionUserId) {
        if (!"message".equals(type) || data == null) {
            return data;
        }
        try {
            Map<String, Object> map = objectMapper.convertValue(data, Map.class);
            Map<String, Object> sessionMap = new LinkedHashMap<>(map);
            Object senderId = sessionMap.get("senderId");
            boolean isMine = senderId != null && Objects.equals(String.valueOf(senderId), String.valueOf(sessionUserId));
            sessionMap.put("isMine", isMine);
            return sessionMap;
        } catch (Exception e) {
            log.warn("按会话补充isMine失败，回退原消息: {}", e.getMessage());
            return data;
        }
    }

    /**
     * 会话信息内部类
     */
    private static class SessionInfo {
        final Long roomId;
        final Long userId;

        SessionInfo(Long roomId, Long userId) {
            this.roomId = roomId;
            this.userId = userId;
        }
    }
}

