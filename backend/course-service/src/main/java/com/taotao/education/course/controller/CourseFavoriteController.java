package com.taotao.education.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.course.service.CourseFavoriteService;
import com.taotao.education.course.vo.CourseFavoriteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程收藏控制器
 */
@Tag(name = "课程收藏", description = "课程收藏相关接口")
@RestController
@RequestMapping("/api/course/favorite")
@RequiredArgsConstructor
public class CourseFavoriteController {

    private final CourseFavoriteService favoriteService;

    @Operation(summary = "添加收藏")
    @PostMapping("/add/{courseId}")
    public Result<Void> addFavorite(@RequestHeader("X-User-Id") Long userId,
                                    @PathVariable Long courseId) {
        favoriteService.addFavorite(userId, courseId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/remove/{courseId}")
    public Result<Void> removeFavorite(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable Long courseId) {
        favoriteService.removeFavorite(userId, courseId);
        return Result.success();
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{courseId}")
    public Result<Boolean> checkFavorite(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long courseId) {
        boolean isFavorite = favoriteService.isFavorite(userId, courseId);
        return Result.success(isFavorite);
    }

    @Operation(summary = "获取收藏列表")
    @GetMapping("/list")
    public Result<Page<CourseFavoriteVO>> getFavorites(@RequestHeader("X-User-Id") Long userId,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CourseFavoriteVO> page = favoriteService.getUserFavorites(userId, pageNum, pageSize);
        return Result.success(page);
    }
}

