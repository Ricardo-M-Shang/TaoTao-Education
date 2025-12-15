package com.taotao.education.course.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.course.dto.StudyProgressDTO;
import com.taotao.education.course.service.StudyRecordService;
import com.taotao.education.course.vo.RecentStudyRecordVO;
import com.taotao.education.course.vo.StudyRecordVO;
import com.taotao.education.course.vo.StudyStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习记录控制器
 */
@Tag(name = "学习记录", description = "学习记录相关接口")
@RestController
@RequestMapping("/api/course/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyRecordService studyRecordService;

    @Operation(summary = "更新学习进度")
    @PostMapping("/progress")
    public Result<Void> updateProgress(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody StudyProgressDTO dto) {
        studyRecordService.updateProgress(userId, dto);
        return Result.success();
    }

    @Operation(summary = "获取课程学习记录")
    @GetMapping("/records/{courseId}")
    public Result<List<StudyRecordVO>> getCourseRecords(@RequestHeader("X-User-Id") Long userId,
                                                        @PathVariable Long courseId) {
        List<StudyRecordVO> records = studyRecordService.getCourseStudyRecords(userId, courseId);
        return Result.success(records);
    }

    @Operation(summary = "获取课时学习记录")
    @GetMapping("/record/lesson/{lessonId}")
    public Result<StudyRecordVO> getLessonRecord(@RequestHeader("X-User-Id") Long userId,
                                                 @PathVariable Long lessonId) {
        StudyRecordVO record = studyRecordService.getLessonStudyRecord(userId, lessonId);
        return Result.success(record);
    }

    @Operation(summary = "获取课程学习进度")
    @GetMapping("/progress/{courseId}")
    public Result<Integer> getCourseProgress(@RequestHeader("X-User-Id") Long userId,
                                             @PathVariable Long courseId) {
        int progress = studyRecordService.calculateCourseProgress(userId, courseId);
        return Result.success(progress);
    }

    @Operation(summary = "获取学习统计数据")
    @GetMapping("/statistics")
    public Result<StudyStatisticsVO> getStatistics(@RequestHeader("X-User-Id") Long userId) {
        StudyStatisticsVO statistics = studyRecordService.getStudyStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取最近学习记录")
    @GetMapping("/recent")
    public Result<List<RecentStudyRecordVO>> getRecentRecords(@RequestHeader("X-User-Id") Long userId,
                                                              @RequestParam(defaultValue = "5") Integer limit) {
        List<RecentStudyRecordVO> records = studyRecordService.getRecentStudyRecords(userId, limit);
        return Result.success(records);
    }
}

