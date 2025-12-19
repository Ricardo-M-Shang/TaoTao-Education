package com.taotao.education.ai.client;

import com.taotao.education.ai.config.DeepSeekConfig;
import com.taotao.education.ai.dto.ChatMessage;
import com.taotao.education.ai.dto.ChatRequest;
import com.taotao.education.ai.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * DeepSeek AI 客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    private final WebClient webClient;
    private final DeepSeekConfig config;

    /**
     * 发送聊天请求
     *
     * @param messages 消息列表
     * @return 响应内容
     */
    public String chat(List<ChatMessage> messages) {
        ChatRequest request = ChatRequest.builder()
                .model(config.getModel())
                .messages(messages)
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .stream(false)
                .build();

        try {
            log.debug("发送 DeepSeek 请求: model={}, messages={}", config.getModel(), messages.size());

            ChatResponse response = webClient.post()
                    .uri(config.getBaseUrl() + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block();

            if (response != null) {
                String content = response.getContent();
                log.debug("DeepSeek 响应: tokens={}", 
                        response.getUsage() != null ? response.getUsage().getTotalTokens() : "unknown");
                return content;
            }

            log.warn("DeepSeek 响应为空");
            return null;

        } catch (WebClientResponseException e) {
            log.error("DeepSeek API 调用失败: status={}, body={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("DeepSeek API 调用异常", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步发送聊天请求
     *
     * @param messages 消息列表
     * @return 响应内容 Mono
     */
    public Mono<String> chatAsync(List<ChatMessage> messages) {
        ChatRequest request = ChatRequest.builder()
                .model(config.getModel())
                .messages(messages)
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .stream(false)
                .build();

        log.debug("异步发送 DeepSeek 请求: model={}, messages={}", config.getModel(), messages.size());

        return webClient.post()
                .uri(config.getBaseUrl() + "/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .map(response -> {
                    log.debug("DeepSeek 异步响应: tokens={}", 
                            response.getUsage() != null ? response.getUsage().getTotalTokens() : "unknown");
                    return response.getContent();
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("DeepSeek API 异步调用失败: status={}, body={}", 
                            e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.error(new RuntimeException("AI 服务调用失败: " + e.getMessage()));
                });
    }

    /**
     * 简单聊天 - 单条用户消息
     *
     * @param systemPrompt 系统提示
     * @param userMessage  用户消息
     * @return 响应内容
     */
    public String chat(String systemPrompt, String userMessage) {
        return chat(List.of(
                ChatMessage.system(systemPrompt),
                ChatMessage.user(userMessage)
        ));
    }
}

