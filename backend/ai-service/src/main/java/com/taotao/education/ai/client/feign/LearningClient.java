package com.taotao.education.ai.client.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.taotao.education.ai.client.feign.fallback.LearningClientFallback;
import com.taotao.education.ai.vo.LearningRecordVO;
import com.taotao.education.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Primary
@FeignClient(name = "learning-service", contextId = "learningClient", fallback = LearningClientFallback.class)
public interface LearningClient {

    @GetMapping("/api/learning/study/recent")
    Result<List<LearningRecordVO>> getRecentLearningRecords(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("limit") Integer limit
    );

    @GetMapping("/api/learning/stats/courses")
    Result<JsonNode> getLearnedCourses(
            @RequestHeader("X-User-Id") String userId
    );

    @GetMapping("/api/learning/report")
    Result<Map<String, Object>> getLearningReport(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("type") String type
    );
}
