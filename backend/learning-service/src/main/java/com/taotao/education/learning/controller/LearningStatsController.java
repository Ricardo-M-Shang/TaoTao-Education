package com.taotao.education.learning.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.learning.service.LearningStatsService;
import com.taotao.education.learning.vo.LearningStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学习统计控制器
 */
@Tag(name = "学习统计", description = "学习统计数据相关接口")
@RestController
@RequestMapping("/api/learning/stats")
@RequiredArgsConstructor
public class LearningStatsController {

    private final LearningStatsService learningStatsService;

    @Operation(summary = "获取课程学习统计")
    @GetMapping("/course/{courseId}")
    public Result<LearningStatsVO> getCourseStats(@RequestHeader("X-User-Id") Long userId,
                                                  @PathVariable Long courseId) {
        LearningStatsVO stats = learningStatsService.getCourseStats(userId, courseId);
        return Result.success(stats);
    }

    @Operation(summary = "获取用户所有课程学习统计")
    @GetMapping("/courses")
    public Result<List<LearningStatsVO>> getUserCourseStats(@RequestHeader("X-User-Id") Long userId) {
        List<LearningStatsVO> statsList = learningStatsService.getUserCourseStats(userId);
        return Result.success(statsList);
    }

    @Operation(summary = "获取正在学习的课程")
    @GetMapping("/learning")
    public Result<List<LearningStatsVO>> getLearningCourses(@RequestHeader("X-User-Id") Long userId) {
        List<LearningStatsVO> courses = learningStatsService.getLearningCourses(userId);
        return Result.success(courses);
    }

    @Operation(summary = "获取已完成的课程")
    @GetMapping("/completed")
    public Result<List<LearningStatsVO>> getCompletedCourses(@RequestHeader("X-User-Id") Long userId) {
        List<LearningStatsVO> courses = learningStatsService.getCompletedCourses(userId);
        return Result.success(courses);
    }

    @Operation(summary = "获取课程完成率分布")
    @GetMapping("/completion-distribution")
    public Result<List<Map<String, Object>>> getCompletionRateDistribution(@RequestHeader("X-User-Id") Long userId) {
        List<Map<String, Object>> distribution = learningStatsService.getCompletionRateDistribution(userId);
        return Result.success(distribution);
    }

    @Operation(summary = "手动更新课程统计")
    @PostMapping("/course/{courseId}/update")
    public Result<Void> updateCourseStats(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long courseId) {
        learningStatsService.updateCourseStats(userId, courseId);
        return Result.success();
    }
}
