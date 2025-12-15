package com.taotao.education.chat.controller;

import com.taotao.education.chat.dto.SendMessageDTO;
import com.taotao.education.chat.service.ChatMessageService;
import com.taotao.education.chat.vo.ChatMessageVO;
import com.taotao.education.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息控制器
 */
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "聊天消息管理")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "发送消息")
    @PostMapping
    public Result<ChatMessageVO> sendMessage(@RequestHeader("X-User-Id") Long userId,
                                             @Valid @RequestBody SendMessageDTO dto) {
        ChatMessageVO message = chatMessageService.sendMessage(userId, dto);
        return Result.success(message);
    }

    @Operation(summary = "获取聊天室消息（分页）")
    @GetMapping("/room/{roomId}")
    public Result<List<ChatMessageVO>> getRoomMessages(@RequestHeader("X-User-Id") Long userId,
                                                        @PathVariable Long roomId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "50") int size) {
        List<ChatMessageVO> messages = chatMessageService.getRoomMessages(userId, roomId, page, size);
        return Result.success(messages);
    }

    @Operation(summary = "获取历史消息（某消息之前）")
    @GetMapping("/room/{roomId}/before/{beforeId}")
    public Result<List<ChatMessageVO>> getMessagesBefore(@RequestHeader("X-User-Id") Long userId,
                                                          @PathVariable Long roomId,
                                                          @PathVariable Long beforeId,
                                                          @RequestParam(defaultValue = "20") int limit) {
        List<ChatMessageVO> messages = chatMessageService.getMessagesBefore(userId, roomId, beforeId, limit);
        return Result.success(messages);
    }

    @Operation(summary = "撤回消息")
    @PostMapping("/{messageId}/recall")
    public Result<Void> recallMessage(@RequestHeader("X-User-Id") Long userId,
                                      @PathVariable Long messageId) {
        chatMessageService.recallMessage(userId, messageId);
        return Result.success();
    }
}

