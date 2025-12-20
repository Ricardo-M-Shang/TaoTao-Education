package com.taotao.education.ai.client.feign;

import com.taotao.education.ai.client.feign.fallback.CourseClientFallback;
import com.taotao.education.ai.vo.CourseVO;
import com.taotao.education.common.result.PageResult;
import com.taotao.education.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Primary
@FeignClient(name = "course-service", contextId = "courseClient", fallback = CourseClientFallback.class)
public interface CourseClient {

    @GetMapping("/api/course/list")
    Result<PageResult<CourseVO>> getCourseList(
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "categoryId", required = false) Long categoryId
    );

    @GetMapping("/api/course/{id}")
    Result<CourseVO> getCourseById(@PathVariable("id") Long id);

    @GetMapping("/api/course/category/list")
    Result<List<Map<String, Object>>> getCategoryList();
}
