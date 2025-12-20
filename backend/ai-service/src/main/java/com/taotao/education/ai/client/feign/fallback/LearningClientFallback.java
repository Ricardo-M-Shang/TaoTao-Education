package com.taotao.education.ai.client.feign.fallback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.education.ai.client.feign.LearningClient;
import com.taotao.education.ai.vo.LearningRecordVO;
import com.taotao.education.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LearningClientFallback implements LearningClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result<List<LearningRecordVO>> getRecentLearningRecords(String userId, Integer limit) {
        log.error("调用学习服务 getRecentLearningRecords 触发降级: userId={}", userId);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<JsonNode> getLearnedCourses(String userId) {
        log.error("调用学习服务 getLearnedCourses 触发降级: userId={}", userId);
        return Result.success(objectMapper.createArrayNode());
    }

    @Override
    public Result<Map<String, Object>> getLearningReport(String userId, String type) {
        log.error("调用学习服务 getLearningReport 触发降级: userId={}", userId);
        return Result.success(Collections.emptyMap());
    }
}
