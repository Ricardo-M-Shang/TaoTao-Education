package com.taotao.education.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.course.dto.ReviewCreateDTO;
import com.taotao.education.course.service.CourseReviewService;
import com.taotao.education.course.vo.CourseReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程评价控制器
 */
@Tag(name = "课程评价", description = "课程评价相关接口")
@RestController
@RequestMapping("/api/course/review")
@RequiredArgsConstructor
public class CourseReviewController {

    private final CourseReviewService reviewService;

    @Operation(summary = "添加评价")
    @PostMapping("/add")
    public Result<Void> addReview(@RequestHeader("X-User-Id") Long userId,
                                  @RequestHeader(value = "X-Username", required = false) String username,
                                  @RequestHeader(value = "X-Nickname", required = false) String nickname,
                                  @RequestHeader(value = "X-Avatar", required = false) String avatar,
                                  @Valid @RequestBody ReviewCreateDTO dto) {
        reviewService.addReview(userId, username, nickname, avatar, dto);
        return Result.success();
    }

    @Operation(summary = "获取课程评价列表")
    @GetMapping("/list/{courseId}")
    public Result<Page<CourseReviewVO>> getReviews(@PathVariable Long courseId,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CourseReviewVO> page = reviewService.getReviewsByCourse(courseId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "检查是否已评价")
    @GetMapping("/check/{courseId}")
    public Result<Boolean> checkReviewed(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long courseId) {
        boolean reviewed = reviewService.hasReviewed(userId, courseId);
        return Result.success(reviewed);
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable Long reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return Result.success();
    }
}

