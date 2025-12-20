package com.taotao.education.ai.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.education.ai.client.feign.LearningClient;
import com.taotao.education.ai.vo.LearningRecordVO;
import com.taotao.education.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学习服务客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningServiceClient {

    private final LearningClient learningClient;
    private final ObjectMapper objectMapper;

    /**
     * 获取用户最近学习记录
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 学习记录列表
     */
    public List<LearningRecordVO> getRecentLearningRecords(Long userId, int limit) {
        try {
            log.debug("获取用户最近学习记录: userId={}, limit={}", userId, limit);

            Result<List<LearningRecordVO>> result = learningClient.getRecentLearningRecords(String.valueOf(userId), limit);

            if (result != null && result.getData() != null) {
                return result.getData();
            }

        } catch (Exception e) {
            log.error("获取学习记录异常", e);
        }
        return Collections.emptyList();
    }

    /**
     * 获取用户已学习的课程ID列表
     *
     * @param userId 用户ID
     * @return 课程ID集合
     */
    public Set<Long> getLearnedCourseIds(Long userId) {
        try {
            log.debug("获取用户已学习课程: userId={}", userId);

            Result<JsonNode> result = learningClient.getLearnedCourses(String.valueOf(userId));

            if (result != null && result.getData() != null) {
                JsonNode data = result.getData();
                // 处理分页响应
                if (data.has("records") && data.get("records").isArray()) {
                    return extractCourseIds(data.get("records"));
                }
                // 处理列表响应
                if (data.isArray()) {
                    return extractCourseIds(data);
                }
            }

        } catch (Exception e) {
            log.error("获取已学习课程异常", e);
        }
        return Collections.emptySet();
    }

    /**
     * 获取用户学习统计
     *
     * @param userId 用户ID
     * @return 学习统计信息
     */
    public LearningStats getLearningStats(Long userId) {
        try {
            log.debug("获取用户学习统计: userId={}", userId);

            Result<Map<String, Object>> result = learningClient.getLearningReport(String.valueOf(userId), "summary");

            if (result != null && result.getData() != null) {
                Map<String, Object> data = result.getData();
                LearningStats stats = new LearningStats();
                stats.setTotalCourses(objectMapper.convertValue(data.get("studyCourseCount"), Integer.class));
                stats.setTotalDuration(objectMapper.convertValue(data.get("totalStudyDuration"), Long.class));
                stats.setTotalDays(objectMapper.convertValue(data.get("totalStudyDays"), Integer.class));
                return stats;
            }

        } catch (Exception e) {
            log.error("获取学习统计异常", e);
        }
        return new LearningStats();
    }

    private Set<Long> extractCourseIds(JsonNode array) {
        return objectMapper.convertValue(array, new TypeReference<List<JsonNode>>() {})
                .stream()
                .map(node -> {
                    JsonNode courseIdNode = node.get("courseId");
                    if (courseIdNode != null) {
                        return courseIdNode.asLong();
                    }
                    JsonNode idNode = node.get("id");
                    if (idNode != null) {
                        return idNode.asLong();
                    }
                    return null;
                })
                .filter(id -> id != null)
                .collect(Collectors.toSet());
    }

    /**
     * 学习统计
     */
    @lombok.Data
    public static class LearningStats {
        private int totalCourses;
        private long totalDuration;
        private int totalDays;
    }
}
