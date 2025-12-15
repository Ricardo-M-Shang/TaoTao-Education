package com.taotao.education.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天室邀请VO
 */
@Data
@Schema(description = "聊天室邀请")
public class ChatInvitationVO {

    @Schema(description = "邀请ID")
    private Long id;

    @Schema(description = "聊天室ID")
    private Long roomId;

    @Schema(description = "聊天室名称")
    private String roomName;

    @Schema(description = "邀请人ID")
    private Long inviterId;

    @Schema(description = "邀请人名称")
    private String inviterName;

    @Schema(description = "邀请人头像")
    private String inviterAvatar;

    @Schema(description = "状态 0-待处理 1-已接受 2-已拒绝 3-已过期")
    private Integer status;

    @Schema(description = "邀请消息")
    private String message;

    @Schema(description = "邀请时间")
    private LocalDateTime createTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
}

