package com.taotao.education.chat.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redis Pub/Sub 广播消息
 * 用于多实例间的 WebSocket 消息广播
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisBroadcastMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息数据（JSON 字符串）
     */
    private String data;

    /**
     * 聊天室ID
     */
    private Long roomId;

    /**
     * 发送实例的唯一标识（用于避免自己接收自己的广播）
     */
    private String instanceId;
}

