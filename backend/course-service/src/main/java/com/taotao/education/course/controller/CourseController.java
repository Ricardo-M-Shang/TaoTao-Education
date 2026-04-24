package com.taotao.education.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.course.dto.CourseCreateDTO;
import com.taotao.education.course.dto.CourseQueryDTO;
import com.taotao.education.course.dto.CourseUpdateDTO;
import com.taotao.education.course.service.CourseService;
import com.taotao.education.course.vo.CourseDetailVO;
import com.taotao.education.course.vo.CourseListVO;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程控制器
 */
@Tag(name = "课程管理", description = "课程CRUD接口")
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "分页查询课程列表")
    @GetMapping("/list")
    @SentinelResource(value = "course:list", blockHandler = "listBlockHandler")
    public Result<Page<CourseListVO>> list(CourseQueryDTO queryDTO) {
        Page<CourseListVO> page = courseService.pageList(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/detail/{courseId}")
    @SentinelResource(value = "course:detail", blockHandler = "detailBlockHandler")
    public Result<CourseDetailVO> detail(@PathVariable Long courseId) {
        CourseDetailVO detail = courseService.getDetail(courseId);
        return Result.success(detail);
    }

    public Result<Page<CourseListVO>> listBlockHandler(CourseQueryDTO queryDTO, BlockException ex) {
        return Result.fail("课程列表请求过于频繁，请稍后重试");
    }

    public Result<CourseDetailVO> detailBlockHandler(Long courseId, BlockException ex) {
        return Result.fail("课程详情请求过于频繁，请稍后重试");
    }

    @Operation(summary = "创建课程")
    @PostMapping("/create")
    public Result<Long> create(@RequestHeader("X-User-Id") Long userId,
                               @Valid @RequestBody CourseCreateDTO createDTO) {
        Long courseId = courseService.createCourse(userId, createDTO);
        return Result.success(courseId);
    }

    @Operation(summary = "更新课程")
    @PutMapping("/update/{courseId}")
    public Result<Void> update(@PathVariable Long courseId,
                               @Valid @RequestBody CourseUpdateDTO updateDTO) {
        courseService.updateCourse(courseId, updateDTO);
        return Result.success();
    }

    @Operation(summary = "发布课程")
    @PostMapping("/publish/{courseId}")
    public Result<Void> publish(@PathVariable Long courseId) {
        courseService.publishCourse(courseId);
        return Result.success();
    }

    @Operation(summary = "下架课程")
    @PostMapping("/offline/{courseId}")
    public Result<Void> offline(@PathVariable Long courseId) {
        courseService.offlineCourse(courseId);
        return Result.success();
    }
}

