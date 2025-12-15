package com.taotao.education.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送消息DTO
 */
@Data
@Schema(description = "发送消息请求")
public class SendMessageDTO {

    @Schema(description = "聊天室ID")
    @NotNull(message = "聊天室ID不能为空")
    private Long roomId;

    @Schema(description = "消息类型 1-文本 2-图片 3-文件 4-系统消息")
    private Integer messageType = 1;

    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @Schema(description = "附件URL")
    private String attachmentUrl;

    @Schema(description = "附件名称")
    private String attachmentName;

    @Schema(description = "附件大小")
    private Long attachmentSize;

    @Schema(description = "回复的消息ID")
    private Long replyToId;

    @Schema(description = "@提及的用户ID列表，逗号分隔")
    private String mentionedUserIds;
}

