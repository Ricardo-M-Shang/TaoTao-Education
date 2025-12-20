package com.taotao.education.ai.client.feign.fallback;

import com.taotao.education.ai.client.feign.CourseClient;
import com.taotao.education.ai.vo.CourseVO;
import com.taotao.education.common.result.PageResult;
import com.taotao.education.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CourseClientFallback implements CourseClient {

    @Override
    public Result<PageResult<CourseVO>> getCourseList(Integer pageSize, Integer status, Long categoryId) {
        log.error("调用课程服务 getCourseList 触发降级");
        PageResult<CourseVO> emptyPage = new PageResult<>();
        emptyPage.setRecords(Collections.emptyList());
        emptyPage.setTotal(0L);
        return Result.success(emptyPage);
    }

    @Override
    public Result<CourseVO> getCourseById(Long id) {
        log.error("调用课程服务 getCourseById 触发降级: id={}", id);
        return Result.success(null);
    }

    @Override
    public Result<List<Map<String, Object>>> getCategoryList() {
        log.error("调用课程服务 getCategoryList 触发降级");
        return Result.success(Collections.emptyList());
    }
}
