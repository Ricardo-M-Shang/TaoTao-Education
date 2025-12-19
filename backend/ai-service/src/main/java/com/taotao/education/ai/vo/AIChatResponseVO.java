package com.taotao.education.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI助手对话响应")
public class AIChatResponseVO {
    
    @Schema(description = "AI回复内容")
    private String reply;
}
