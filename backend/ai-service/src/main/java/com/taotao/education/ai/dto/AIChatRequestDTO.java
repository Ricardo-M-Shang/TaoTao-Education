package com.taotao.education.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI助手对话请求")
public class AIChatRequestDTO {
    
    @Schema(description = "用户消息", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("message")
    private String message;
    
    @Schema(description = "角色身份: 1-学生, 2-老师", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("role")
    private Integer role;
}
