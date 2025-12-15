package com.taotao.education.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.course.dto.CourseAuditDTO;
import com.taotao.education.course.dto.CourseQueryDTO;
import com.taotao.education.course.service.CourseService;
import com.taotao.education.course.vo.CourseListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 机构端课程审核/管理
 */
@Tag(name = "机构课程管理", description = "机构端课程审核/上下架接口")
@RestController
@RequestMapping("/api/course/org")
@RequiredArgsConstructor
public class OrgCourseController {

    private final CourseService courseService;

    @Operation(summary = "机构分页查询课程（默认待审核）")
    @GetMapping("/list")
    public Result<Page<CourseListVO>> list(@RequestHeader("X-User-Id") Long orgId,
                                           CourseQueryDTO queryDTO) {
        Page<CourseListVO> page = courseService.pageOrgCourses(orgId, queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "机构审核通过课程")
    @PostMapping("/approve/{courseId}")
    public Result<Void> approve(@RequestHeader("X-User-Id") Long orgId,
                                @RequestHeader(value = "X-User-Name", required = false) String auditorName,
                                @PathVariable Long courseId,
                                @Valid @RequestBody(required = false) CourseAuditDTO dto) {
        courseService.approveCourse(orgId, orgId, auditorName, courseId, dto == null ? null : dto.getRemark());
        return Result.success();
    }

    @Operation(summary = "机构审核拒绝课程")
    @PostMapping("/reject/{courseId}")
    public Result<Void> reject(@RequestHeader("X-User-Id") Long orgId,
                               @RequestHeader(value = "X-User-Name", required = false) String auditorName,
                               @PathVariable Long courseId,
                               @Valid @RequestBody(required = false) CourseAuditDTO dto) {
        courseService.rejectCourse(orgId, orgId, auditorName, courseId, dto == null ? null : dto.getRemark());
        return Result.success();
    }

    @Operation(summary = "机构下架课程")
    @PostMapping("/offline/{courseId}")
    public Result<Void> offline(@RequestHeader("X-User-Id") Long orgId,
                                @PathVariable Long courseId) {
        courseService.offlineCourseByOrg(orgId, courseId);
        return Result.success();
    }

    @Operation(summary = "机构讲师下拉")
    @GetMapping("/teachers")
    public Result<java.util.List<com.taotao.education.course.vo.TeacherOptionVO>> teachers(@RequestHeader("X-User-Id") Long orgId) {
        return Result.success(courseService.listOrgTeachers(orgId));
    }

    @Operation(summary = "机构按讲师筛选课程")
    @GetMapping("/list/by-teacher")
    public Result<Page<CourseListVO>> listByTeacher(@RequestHeader("X-User-Id") Long orgId,
                                                    @RequestParam Long teacherId,
                                                    CourseQueryDTO queryDTO) {
        queryDTO.setTeacherId(teacherId);
        Page<CourseListVO> page = courseService.pageOrgCourses(orgId, queryDTO);
        return Result.success(page);
    }
}


