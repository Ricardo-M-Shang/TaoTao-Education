package com.taotao.education.chat.controller;

import com.taotao.education.chat.service.ChatMemberService;
import com.taotao.education.chat.vo.ChatMemberVO;
import com.taotao.education.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天室成员控制器
 */
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "聊天室成员管理")
public class ChatMemberController {

    private final ChatMemberService chatMemberService;

    @Operation(summary = "获取聊天室成员列表")
    @GetMapping("/room/{roomId}")
    public Result<List<ChatMemberVO>> getRoomMembers(@PathVariable Long roomId) {
        List<ChatMemberVO> members = chatMemberService.getRoomMembers(roomId);
        return Result.success(members);
    }

    @Operation(summary = "退出聊天室")
    @PostMapping("/room/{roomId}/leave")
    public Result<Void> leaveRoom(@RequestHeader("X-User-Id") Long userId,
                                  @PathVariable Long roomId) {
        chatMemberService.leaveRoom(userId, roomId);
        return Result.success();
    }

    @Operation(summary = "移除成员")
    @DeleteMapping("/room/{roomId}/user/{targetUserId}")
    public Result<Void> removeMember(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable Long roomId,
                                     @PathVariable Long targetUserId) {
        chatMemberService.removeMember(userId, roomId, targetUserId);
        return Result.success();
    }

    @Operation(summary = "设置/取消管理员")
    @PostMapping("/room/{roomId}/user/{targetUserId}/admin")
    public Result<Void> setAdmin(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable Long roomId,
                                 @PathVariable Long targetUserId,
                                 @RequestParam boolean isAdmin) {
        chatMemberService.setAdmin(userId, roomId, targetUserId, isAdmin);
        return Result.success();
    }

    @Operation(summary = "禁言/解禁成员")
    @PostMapping("/room/{roomId}/user/{targetUserId}/mute")
    public Result<Void> muteMember(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long roomId,
                                   @PathVariable Long targetUserId,
                                   @RequestParam boolean mute) {
        chatMemberService.muteMember(userId, roomId, targetUserId, mute);
        return Result.success();
    }

    @Operation(summary = "更新我的设置（置顶/免打扰）")
    @PutMapping("/room/{roomId}/settings")
    public Result<Void> updateSettings(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable Long roomId,
                                       @RequestParam(required = false) Boolean isPinned,
                                       @RequestParam(required = false) Boolean isMuted) {
        chatMemberService.updateMemberSettings(userId, roomId, isPinned, isMuted);
        return Result.success();
    }

    @Operation(summary = "清空未读消息")
    @PostMapping("/room/{roomId}/read")
    public Result<Void> clearUnread(@RequestHeader("X-User-Id") Long userId,
                                    @PathVariable Long roomId) {
        chatMemberService.clearUnreadCount(userId, roomId);
        return Result.success();
    }
}

