package com.taotao.education.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 邀请成员DTO
 */
@Data
@Schema(description = "邀请成员请求")
public class InviteMembersDTO {

    @Schema(description = "聊天室ID")
    @NotNull(message = "聊天室ID不能为空")
    private Long roomId;

    @Schema(description = "被邀请用户ID列表")
    @NotEmpty(message = "被邀请用户不能为空")
    private List<Long> inviteeIds;

    @Schema(description = "邀请消息")
    private String message;
}

