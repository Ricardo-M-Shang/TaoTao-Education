package com.taotao.education.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.course.dto.CourseCreateDTO;
import com.taotao.education.course.dto.CourseQueryDTO;
import com.taotao.education.course.dto.CourseUpdateDTO;
import com.taotao.education.course.dto.ChapterCreateDTO;
import com.taotao.education.course.dto.ChapterUpdateDTO;
import com.taotao.education.course.dto.LessonCreateDTO;
import com.taotao.education.course.dto.LessonUpdateDTO;
import com.taotao.education.course.service.CourseService;
import com.taotao.education.course.vo.CourseListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 讲师端课程管理
 */
@Tag(name = "讲师课程管理", description = "讲师端课程发布/管理接口")
@RestController
@RequestMapping("/api/course/teacher")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseService courseService;

    @Operation(summary = "讲师创建课程")
    @PostMapping("/create")
    public Result<Long> create(@RequestHeader("X-User-Id") Long teacherId,
                               @Valid @RequestBody CourseCreateDTO dto) {
        Long id = courseService.createCourse(teacherId, dto);
        return Result.success(id);
    }

    @Operation(summary = "讲师更新课程")
    @PutMapping("/update/{courseId}")
    public Result<Void> update(@RequestHeader("X-User-Id") Long teacherId,
                               @PathVariable Long courseId,
                               @Valid @RequestBody CourseUpdateDTO dto) {
        courseService.updateCourse(courseId, dto);
        return Result.success();
    }

    @Operation(summary = "讲师发布课程")
    @PostMapping("/publish/{courseId}")
    public Result<Void> publish(@RequestHeader("X-User-Id") Long teacherId,
                                @PathVariable Long courseId) {
        courseService.updateCourseStatus(teacherId, courseId, 1);
        return Result.success();
    }

    @Operation(summary = "讲师下架课程")
    @PostMapping("/offline/{courseId}")
    public Result<Void> offline(@RequestHeader("X-User-Id") Long teacherId,
                                @PathVariable Long courseId) {
        courseService.updateCourseStatus(teacherId, courseId, 3);
        return Result.success();
    }

    @Operation(summary = "讲师分页查询课程列表")
    @GetMapping("/list")
    public Result<Page<CourseListVO>> list(@RequestHeader("X-User-Id") Long teacherId,
                                           CourseQueryDTO queryDTO) {
        Page<CourseListVO> page = courseService.pageTeacherCourses(teacherId, queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "讲师创建章节")
    @PostMapping("/chapter")
    public Result<Long> createChapter(@RequestHeader("X-User-Id") Long teacherId,
                                      @Valid @RequestBody ChapterCreateDTO dto) {
        Long id = courseService.createChapter(teacherId, dto);
        return Result.success(id);
    }

    @Operation(summary = "讲师更新章节")
    @PutMapping("/chapter/{chapterId}")
    public Result<Void> updateChapter(@RequestHeader("X-User-Id") Long teacherId,
                                      @PathVariable Long chapterId,
                                      @Valid @RequestBody ChapterUpdateDTO dto) {
        courseService.updateChapter(teacherId, chapterId, dto);
        return Result.success();
    }

    @Operation(summary = "讲师删除章节")
    @DeleteMapping("/chapter/{chapterId}")
    public Result<Void> deleteChapter(@RequestHeader("X-User-Id") Long teacherId,
                                      @PathVariable Long chapterId) {
        courseService.deleteChapter(teacherId, chapterId);
        return Result.success();
    }

    @Operation(summary = "讲师创建课时")
    @PostMapping("/lesson")
    public Result<Long> createLesson(@RequestHeader("X-User-Id") Long teacherId,
                                     @Valid @RequestBody LessonCreateDTO dto) {
        Long id = courseService.createLesson(teacherId, dto);
        return Result.success(id);
    }

    @Operation(summary = "讲师更新课时")
    @PutMapping("/lesson/{lessonId}")
    public Result<Void> updateLesson(@RequestHeader("X-User-Id") Long teacherId,
                                     @PathVariable Long lessonId,
                                     @Valid @RequestBody LessonUpdateDTO dto) {
        courseService.updateLesson(teacherId, lessonId, dto);
        return Result.success();
    }

    @Operation(summary = "讲师删除课时")
    @DeleteMapping("/lesson/{lessonId}")
    public Result<Void> deleteLesson(@RequestHeader("X-User-Id") Long teacherId,
                                     @PathVariable Long lessonId) {
        courseService.deleteLesson(teacherId, lessonId);
        return Result.success();
    }
}

