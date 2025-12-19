package com.taotao.education.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek Chat 响应
 */
@Data
public class ChatResponse {

    private String id;

    private String object;

    private Long created;

    private String model;

    private List<Choice> choices;

    private Usage usage;

    @Data
    public static class Choice {

        private Integer index;

        private ChatMessage message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    public static class Usage {

        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    /**
     * 获取助手回复内容
     */
    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            ChatMessage message = choices.get(0).getMessage();
            if (message != null) {
                return message.getContent();
            }
        }
        return null;
    }
}

