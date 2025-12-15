package com.taotao.education.course.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.course.service.CourseService;
import com.taotao.education.course.vo.OpsCourseOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营端课程统计接口
 */
@Tag(name = "运营-课程统计", description = "运营端课程概览接口")
@RestController
@RequestMapping("/api/course/ops")
@RequiredArgsConstructor
public class OpsCourseController {

    private final CourseService courseService;

    @Operation(summary = "课程概览")
    @GetMapping("/overview")
    public Result<OpsCourseOverviewVO> overview() {
        return Result.success(courseService.getOpsOverview());
    }
}


