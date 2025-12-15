package com.taotao.education.chat.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息事件（用于消息队列传输）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 聊天室ID
     */
    private Long roomId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderName;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 发送者角色
     */
    private Integer senderRole;

    /**
     * 消息类型 1-文本 2-图片 3-文件 4-系统消息
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 附件名称
     */
    private String attachmentName;

    /**
     * 附件大小
     */
    private Long attachmentSize;

    /**
     * 回复的消息ID
     */
    private Long replyToId;

    /**
     * 回复的消息内容预览
     */
    private String replyToContent;

    /**
     * @提及的用户ID列表
     */
    private String mentionedUserIds;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 新消息
         */
        NEW_MESSAGE,
        
        /**
         * 消息撤回
         */
        RECALL_MESSAGE,
        
        /**
         * 用户加入
         */
        USER_JOIN,
        
        /**
         * 用户离开
         */
        USER_LEAVE,
        
        /**
         * 用户在线状态变更
         */
        USER_STATUS_CHANGE
    }
}

