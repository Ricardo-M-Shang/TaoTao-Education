package com.taotao.education.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek Chat 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    private List<ChatMessage> messages;

    /**
     * 最大token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 是否流式响应
     */
    private Boolean stream;
}

