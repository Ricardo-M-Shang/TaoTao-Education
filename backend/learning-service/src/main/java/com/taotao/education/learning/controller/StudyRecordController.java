package com.taotao.education.learning.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.learning.dto.StudyProgressDTO;
import com.taotao.education.learning.entity.StudyRecord;
import com.taotao.education.learning.service.StudyRecordService;
import com.taotao.education.learning.vo.StudyRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习记录控制器
 */
@Tag(name = "学习记录", description = "学习记录管理相关接口")
@RestController
@RequestMapping("/api/learning/study")
@RequiredArgsConstructor
public class StudyRecordController {

    private final StudyRecordService studyRecordService;

    @Operation(summary = "记录学习进度")
    @PostMapping("/progress")
    public Result<Void> recordProgress(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody StudyProgressDTO dto) {
        studyRecordService.recordStudyProgress(userId, dto);
        return Result.success();
    }

    @Operation(summary = "获取学习记录列表")
    @GetMapping("/records")
    public Result<Page<StudyRecordVO>> getStudyRecords(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<StudyRecordVO> page = studyRecordService.getUserStudyRecords(userId, courseId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取最近学习记录")
    @GetMapping("/recent")
    public Result<List<StudyRecordVO>> getRecentStudyRecords(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<StudyRecordVO> records = studyRecordService.getRecentStudyRecords(userId, limit);
        return Result.success(records);
    }

    @Operation(summary = "获取课程学习进度")
    @GetMapping("/course/{courseId}/progress")
    public Result<Double> getCourseProgress(@RequestHeader("X-User-Id") Long userId,
                                           @PathVariable Long courseId) {
        Double progress = studyRecordService.getCourseProgress(userId, courseId);
        return Result.success(progress);
    }

    @Operation(summary = "获取课时学习进度")
    @GetMapping("/lesson/{lessonId}/progress")
    public Result<StudyRecord> getLessonProgress(@RequestHeader("X-User-Id") Long userId,
                                                @PathVariable Long lessonId) {
        StudyRecord record = studyRecordService.getLessonProgress(userId, lessonId);
        return Result.success(record);
    }
}
