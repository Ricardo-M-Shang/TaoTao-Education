package com.taotao.education.learning.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.learning.dto.LearningReportQueryDTO;
import com.taotao.education.learning.service.LearningReportService;
import com.taotao.education.learning.service.LearningStatsService;
import com.taotao.education.learning.vo.LearningReportVO;
import com.taotao.education.learning.vo.UserLearningStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学习报告控制器
 */
@Tag(name = "学习报告", description = "学习报告与分析相关接口")
@RestController
@RequestMapping("/api/learning/report")
@RequiredArgsConstructor
public class LearningReportController {

    private final LearningReportService learningReportService;
    private final LearningStatsService learningStatsService;

    @Operation(summary = "获取学习报告")
    @GetMapping
    public Result<LearningReportVO> getLearningReport(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "summary") String type) {
        
        LearningReportQueryDTO queryDTO = new LearningReportQueryDTO();
        queryDTO.setPeriod(period);
        queryDTO.setCourseId(courseId);
        queryDTO.setType(type);
        
        LearningReportVO report = learningReportService.generateLearningReport(userId, queryDTO);
        return Result.success(report);
    }

    @Operation(summary = "获取学习时长趋势")
    @GetMapping("/duration-trend")
    public Result<List<Map<String, Object>>> getStudyDurationTrend(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "week") String period) {
        
        List<Map<String, Object>> trend = learningReportService.getStudyDurationTrend(userId, period);
        return Result.success(trend);
    }

    @Operation(summary = "获取学习时段分布")
    @GetMapping("/time-distribution")
    public Result<List<Map<String, Object>>> getStudyTimeDistribution(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "week") String period) {
        
        List<Map<String, Object>> distribution = learningReportService.getStudyTimeDistribution(userId, period);
        return Result.success(distribution);
    }

    @Operation(summary = "获取用户学习统计")
    @GetMapping("/user-stats")
    public Result<UserLearningStatsVO> getUserStats(@RequestHeader("X-User-Id") Long userId) {
        UserLearningStatsVO stats = learningReportService.getUserLearningStats(userId);
        return Result.success(stats);
    }

    @Operation(summary = "获取学习建议")
    @GetMapping("/advice")
    public Result<List<String>> getLearningAdvice(@RequestHeader("X-User-Id") Long userId) {
        List<String> advice = learningReportService.getLearningAdvice(userId);
        return Result.success(advice);
    }

    @Operation(summary = "获取用户学习等级")
    @GetMapping("/level")
    public Result<Integer> getUserLevel(@RequestHeader("X-User-Id") Long userId) {
        Integer level = learningReportService.calculateUserLevel(userId);
        return Result.success(level);
    }

    @Operation(summary = "更新用户学习统计")
    @PostMapping("/user-stats/update")
    public Result<Void> updateUserStats(@RequestHeader("X-User-Id") Long userId) {
        // 通过统计服务触发统计更新
        learningStatsService.updateUserCourseStats(userId);
        return Result.success();
    }
}
