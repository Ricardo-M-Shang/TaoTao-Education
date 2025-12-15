package com.taotao.education.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天消息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chat_message")
public class ChatMessage extends BaseEntity {

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
     * 发送者角色 1-学员 2-讲师
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
     * 附件URL（图片/文件）
     */
    private String attachmentUrl;

    /**
     * 附件名称
     */
    private String attachmentName;

    /**
     * 附件大小（字节）
     */
    private Long attachmentSize;

    /**
     * 回复的消息ID
     */
    private Long replyToId;

    /**
     * 状态 0-已撤回 1-正常
     */
    private Integer status;

    /**
     * @提及的用户ID列表（逗号分隔）
     */
    private String mentionedUserIds;
}

