package com.taotao.education.ai.controller;

import com.taotao.education.ai.dto.RecommendRequestDTO;
import com.taotao.education.ai.service.RecommendService;
import com.taotao.education.ai.vo.RecommendResultVO;
import com.taotao.education.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 课程推荐控制器
 */
@Slf4j
@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
@Tag(name = "课程推荐", description = "AI智能课程推荐接口")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping
    @Operation(summary = "获取个性化推荐", description = "根据用户学习历史获取AI个性化课程推荐")
    public Result<RecommendResultVO> getRecommendations(
            @Parameter(description = "推荐数量") @RequestParam(required = false) Integer count,
            @Parameter(description = "是否包含已学习课程") @RequestParam(required = false) Boolean includeLearnedCourses,
            @Parameter(description = "指定分类ID") @RequestParam(required = false) Long categoryId,
            HttpServletRequest request) {

        Long userId = getUserId(request);
        if (userId == null) {
            // 未登录用户返回热门推荐
            return Result.success(recommendService.getPopularRecommendations(count != null ? count : 5));
        }

        RecommendRequestDTO dto = new RecommendRequestDTO();
        dto.setCount(count);
        dto.setIncludeLearnedCourses(includeLearnedCourses);
        dto.setCategoryId(categoryId);

        log.debug("获取个性化推荐: userId={}, count={}", userId, count);
        RecommendResultVO result = recommendService.getRecommendations(userId, dto);
        return Result.success(result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新推荐", description = "清除缓存并重新生成推荐")
    public Result<RecommendResultVO> refreshRecommendations(
            @RequestBody(required = false) RecommendRequestDTO request,
            HttpServletRequest httpRequest) {

        Long userId = getUserId(httpRequest);
        if (userId == null) {
            return Result.fail("请先登录");
        }

        if (request == null) {
            request = new RecommendRequestDTO();
        }

        log.debug("刷新推荐: userId={}", userId);
        RecommendResultVO result = recommendService.refreshRecommendations(userId, request);
        return Result.success(result);
    }

    @GetMapping("/popular")
    @Operation(summary = "获取热门推荐", description = "获取平台热门课程推荐（无需登录）")
    public Result<RecommendResultVO> getPopularRecommendations(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "5") Integer count) {

        log.debug("获取热门推荐: count={}", count);
        RecommendResultVO result = recommendService.getPopularRecommendations(count);
        return Result.success(result);
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

