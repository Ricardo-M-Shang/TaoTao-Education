package com.taotao.education.chat.websocket;

import com.taotao.education.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 从请求参数获取 roomId，userId 优先从 token 解析（避免前端大整数精度丢失）
            String roomIdStr = servletRequest.getServletRequest().getParameter("roomId");
            String userIdStr = servletRequest.getServletRequest().getParameter("userId");
            String token = servletRequest.getServletRequest().getParameter("token");

            if (roomIdStr == null) {
                log.warn("WebSocket握手失败：缺少roomId参数");
                return false;
            }

            try {
                Long roomId = Long.parseLong(roomIdStr);
                Long userId = null;

                // 优先使用 token 解析 userId（更准确）
                if (token != null && !token.isBlank() && JwtUtils.validateToken(token)) {
                    userId = JwtUtils.getUserId(token);
                } else if (userIdStr != null && !userIdStr.isBlank()) {
                    // 兼容旧逻辑：从 userId 参数读取
                    userId = Long.parseLong(userIdStr);
                }

                if (userId == null) {
                    log.warn("WebSocket握手失败：缺少可用的用户身份信息");
                    return false;
                }
                
                attributes.put("roomId", roomId);
                attributes.put("userId", userId);
                
                log.info("WebSocket握手成功：roomId={}, userId={}", roomId, userId);
                return true;
            } catch (NumberFormatException e) {
                log.warn("WebSocket握手失败：参数格式错误");
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后处理
    }
}

