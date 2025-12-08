package com.taotao.education.order.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.order.service.UserCourseService;
import com.taotao.education.order.vo.UserCourseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户课程控制器
 */
@Tag(name = "用户课程", description = "用户已购课程管理")
@RestController
@RequestMapping("/api/order/user-course")
@RequiredArgsConstructor
public class UserCourseController {

    private final UserCourseService userCourseService;

    @Operation(summary = "获取用户已购课程列表")
    @GetMapping("/list")
    public Result<List<UserCourseVO>> getUserCourses(@RequestHeader("X-User-Id") Long userId) {
        List<UserCourseVO> list = userCourseService.getUserCourses(userId);
        return Result.success(list);
    }

    @Operation(summary = "更新学习进度")
    @PostMapping("/progress")
    public Result<Void> updateProgress(@RequestHeader("X-User-Id") Long userId,
                                       @RequestParam Long courseId,
                                       @RequestParam Integer progress) {
        userCourseService.updateProgress(userId, courseId, progress);
        return Result.success();
    }
}

