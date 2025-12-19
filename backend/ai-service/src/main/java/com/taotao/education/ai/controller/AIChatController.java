package com.taotao.education.ai.controller;

import com.taotao.education.ai.dto.AIChatRequestDTO;
import com.taotao.education.ai.service.AIChatService;
import com.taotao.education.ai.vo.AIChatResponseVO;
import com.taotao.education.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "AI助手对话", description = "AI智能助手对话接口")
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping
    @Operation(summary = "发送对话", description = "与AI助手进行对话")
    public Result<AIChatResponseVO> chat(
            @RequestBody AIChatRequestDTO request,
            HttpServletRequest httpRequest) {

        Long userId = getUserId(httpRequest);
        if (userId == null) {
            return Result.fail("请先登录");
        }

        log.debug("AI助手对话: userId={}, role={}", userId, request.getRole());
        AIChatResponseVO response = aiChatService.chat(userId, request);
        return Result.success(response);
    }

    /**
     * 从请求头获取用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID: {}", userIdStr);
            }
        }
        return null;
    }
}
