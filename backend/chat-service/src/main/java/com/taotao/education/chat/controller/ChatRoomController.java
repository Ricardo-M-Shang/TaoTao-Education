package com.taotao.education.chat.controller;

import com.taotao.education.chat.dto.CreateRoomDTO;
import com.taotao.education.chat.service.ChatRoomService;
import com.taotao.education.chat.vo.ChatRoomVO;
import com.taotao.education.chat.vo.CourseStudentVO;
import com.taotao.education.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天室控制器
 */
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Tag(name = "聊天室管理")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "创建聊天室")
    @PostMapping
    public Result<Long> createRoom(@RequestHeader("X-User-Id") Long userId,
                                   @Valid @RequestBody CreateRoomDTO dto) {
        Long roomId = chatRoomService.createRoom(userId, dto);
        return Result.success(roomId);
    }

    @Operation(summary = "获取聊天室详情")
    @GetMapping("/{roomId}")
    public Result<ChatRoomVO> getRoomDetail(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable Long roomId) {
        ChatRoomVO room = chatRoomService.getRoomDetail(userId, roomId);
        return Result.success(room);
    }

    @Operation(summary = "获取我加入的聊天室列表")
    @GetMapping("/my")
    public Result<List<ChatRoomVO>> getMyRooms(@RequestHeader("X-User-Id") Long userId) {
        List<ChatRoomVO> rooms = chatRoomService.getUserRooms(userId);
        return Result.success(rooms);
    }

    @Operation(summary = "获取我创建的聊天室列表")
    @GetMapping("/created")
    public Result<List<ChatRoomVO>> getCreatedRooms(@RequestHeader("X-User-Id") Long userId) {
        List<ChatRoomVO> rooms = chatRoomService.getCreatorRooms(userId);
        return Result.success(rooms);
    }

    @Operation(summary = "根据课程获取聊天室")
    @GetMapping("/course/{courseId}")
    public Result<ChatRoomVO> getRoomByCourse(@RequestHeader("X-User-Id") Long userId,
                                               @PathVariable Long courseId) {
        ChatRoomVO room = chatRoomService.getRoomByCourse(userId, courseId);
        return Result.success(room);
    }

    @Operation(summary = "更新聊天室信息")
    @PutMapping("/{roomId}")
    public Result<Void> updateRoom(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long roomId,
                                   @Valid @RequestBody CreateRoomDTO dto) {
        chatRoomService.updateRoom(userId, roomId, dto);
        return Result.success();
    }

    @Operation(summary = "删除聊天室")
    @DeleteMapping("/{roomId}")
    public Result<Void> deleteRoom(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long roomId) {
        chatRoomService.deleteRoom(userId, roomId);
        return Result.success();
    }

    @Operation(summary = "更新聊天室公告")
    @PutMapping("/{roomId}/announcement")
    public Result<Void> updateAnnouncement(@RequestHeader("X-User-Id") Long userId,
                                           @PathVariable Long roomId,
                                           @RequestBody String announcement) {
        chatRoomService.updateAnnouncement(userId, roomId, announcement);
        return Result.success();
    }

    @Operation(summary = "获取课程学员列表（用于邀请）")
    @GetMapping("/{roomId}/students")
    public Result<List<CourseStudentVO>> getCourseStudents(@RequestHeader("X-User-Id") Long userId,
                                                           @PathVariable Long roomId) {
        List<CourseStudentVO> students = chatRoomService.getCourseStudents(userId, roomId);
        return Result.success(students);
    }
}

