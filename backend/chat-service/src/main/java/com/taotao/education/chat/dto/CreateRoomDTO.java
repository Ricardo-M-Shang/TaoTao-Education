package com.taotao.education.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建聊天室DTO
 */
@Data
@Schema(description = "创建聊天室请求")
public class CreateRoomDTO {

    @Schema(description = "聊天室名称")
    @NotBlank(message = "聊天室名称不能为空")
    private String name;

    @Schema(description = "聊天室描述")
    private String description;

    @Schema(description = "关联课程ID")
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Schema(description = "聊天室封面")
    private String cover;

    @Schema(description = "最大成员数，0表示不限制")
    private Integer maxMembers = 0;

    @Schema(description = "是否需要审批加入")
    private Integer needApproval = 0;

    @Schema(description = "公告")
    private String announcement;
}

