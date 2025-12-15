package com.taotao.education.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息VO
 */
@Data
@Schema(description = "聊天消息")
public class ChatMessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "聊天室ID")
    private Long roomId;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者昵称")
    private String senderName;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "发送者角色 1-学员 2-讲师")
    private Integer senderRole;

    @Schema(description = "消息类型 1-文本 2-图片 3-文件 4-系统消息")
    private Integer messageType;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "附件URL")
    private String attachmentUrl;

    @Schema(description = "附件名称")
    private String attachmentName;

    @Schema(description = "附件大小")
    private Long attachmentSize;

    @Schema(description = "回复的消息ID")
    private Long replyToId;

    @Schema(description = "回复的消息内容预览")
    private String replyToContent;

    @Schema(description = "@提及的用户ID列表")
    private String mentionedUserIds;

    @Schema(description = "发送时间")
    private LocalDateTime createTime;

    @Schema(description = "是否是自己发送的")
    private Boolean isMine;
}

