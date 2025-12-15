package com.taotao.education.chat.controller;

import com.taotao.education.chat.dto.InviteMembersDTO;
import com.taotao.education.chat.service.ChatInvitationService;
import com.taotao.education.chat.vo.ChatInvitationVO;
import com.taotao.education.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天室邀请控制器
 */
@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
@Tag(name = "聊天室邀请管理")
public class ChatInvitationController {

    private final ChatInvitationService chatInvitationService;

    @Operation(summary = "邀请成员")
    @PostMapping
    public Result<Void> inviteMembers(@RequestHeader("X-User-Id") Long userId,
                                      @Valid @RequestBody InviteMembersDTO dto) {
        chatInvitationService.inviteMembers(userId, dto);
        return Result.success();
    }

    @Operation(summary = "邀请课程所有学员")
    @PostMapping("/room/{roomId}/invite-all")
    public Result<Void> inviteAllStudents(@RequestHeader("X-User-Id") Long userId,
                                          @PathVariable Long roomId) {
        chatInvitationService.inviteAllCourseStudents(userId, roomId);
        return Result.success();
    }

    @Operation(summary = "获取待处理的邀请列表")
    @GetMapping("/pending")
    public Result<List<ChatInvitationVO>> getPendingInvitations(@RequestHeader("X-User-Id") Long userId) {
        List<ChatInvitationVO> invitations = chatInvitationService.getPendingInvitations(userId);
        return Result.success(invitations);
    }

    @Operation(summary = "获取我的所有邀请记录")
    @GetMapping("/my")
    public Result<List<ChatInvitationVO>> getMyInvitations(@RequestHeader("X-User-Id") Long userId) {
        List<ChatInvitationVO> invitations = chatInvitationService.getUserInvitations(userId);
        return Result.success(invitations);
    }

    @Operation(summary = "获取待处理邀请数量")
    @GetMapping("/pending/count")
    public Result<Integer> getPendingCount(@RequestHeader("X-User-Id") Long userId) {
        int count = chatInvitationService.getPendingCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "接受邀请")
    @PostMapping("/{invitationId}/accept")
    public Result<Void> acceptInvitation(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long invitationId) {
        chatInvitationService.acceptInvitation(userId, invitationId);
        return Result.success();
    }

    @Operation(summary = "拒绝邀请")
    @PostMapping("/{invitationId}/reject")
    public Result<Void> rejectInvitation(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long invitationId) {
        chatInvitationService.rejectInvitation(userId, invitationId);
        return Result.success();
    }
}

