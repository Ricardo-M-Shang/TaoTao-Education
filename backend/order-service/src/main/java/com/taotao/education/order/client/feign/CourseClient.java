package com.taotao.education.order.client.feign;

import com.taotao.education.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "course-service", contextId = "orderCourseClient")
public interface CourseClient {

    @PostMapping("/api/course/internal/study/increase/{courseId}")
    Result<Void> increaseStudyCount(@PathVariable("courseId") Long courseId,
                                    @RequestParam(value = "count", defaultValue = "1") Integer count);
}
