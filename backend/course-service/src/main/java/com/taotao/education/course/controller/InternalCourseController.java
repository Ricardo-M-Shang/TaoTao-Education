package com.taotao.education.course.controller;

import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.common.result.Result;
import com.taotao.education.course.service.CourseService;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/course/internal")
@RequiredArgsConstructor
public class InternalCourseController {

    private final CourseService courseService;

    @PostMapping("/study/increase/{courseId}")
    public Result<Void> increaseStudyCount(@PathVariable Long courseId,
                                           @RequestParam(defaultValue = "1") Integer count) {
        String xid = RootContext.getXID();
        log.info("课程服务收到分支事务调用: xid={}, courseId={}, count={}", xid, courseId, count);
        if (!StringUtils.hasText(xid)) {
            throw new BusinessException("未检测到Seata全局事务XID，拒绝执行跨服务写操作");
        }
        courseService.increaseStudyCount(courseId, count);
        return Result.success();
    }
}
