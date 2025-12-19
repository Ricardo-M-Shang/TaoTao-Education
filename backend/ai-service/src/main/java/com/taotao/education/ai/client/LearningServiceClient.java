package com.taotao.education.ai.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.education.ai.config.ServiceConfig;
import com.taotao.education.ai.vo.LearningRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学习服务客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningServiceClient {

    private final WebClient webClient;
    private final ServiceConfig serviceConfig;
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

            String response = webClient.get()
                    .uri(serviceConfig.getLearningUrl() + "/api/learning/study/recent?limit=" + limit)
                    .header("X-User-Id", String.valueOf(userId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseLearningRecordsFromResponse(response);

        } catch (WebClientResponseException e) {
            log.error("获取学习记录失败: status={}", e.getStatusCode());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("获取学习记录异常", e);
            return Collections.emptyList();
        }
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

            String response = webClient.get()
                    .uri(serviceConfig.getLearningUrl() + "/api/learning/stats/courses")
                    .header("X-User-Id", String.valueOf(userId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.get("data");
                if (data != null) {
                    // 处理分页响应
                    JsonNode records = data.get("records");
                    if (records != null && records.isArray()) {
                        return extractCourseIds(records);
                    }
                    // 处理列表响应
                    if (data.isArray()) {
                        return extractCourseIds(data);
                    }
                }
            }
            return Collections.emptySet();

        } catch (WebClientResponseException e) {
            log.error("获取已学习课程失败: status={}", e.getStatusCode());
            return Collections.emptySet();
        } catch (Exception e) {
            log.error("获取已学习课程异常", e);
            return Collections.emptySet();
        }
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

            String response = webClient.get()
                    .uri(serviceConfig.getLearningUrl() + "/api/learning/report?type=summary")
                    .header("X-User-Id", String.valueOf(userId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.get("data");
                if (data != null && !data.isNull()) {
                    LearningStats stats = new LearningStats();
                    stats.setTotalCourses(data.path("studyCourseCount").asInt(0));
                    stats.setTotalDuration(data.path("totalStudyDuration").asLong(0));
                    stats.setTotalDays(data.path("totalStudyDays").asInt(0));
                    return stats;
                }
            }
            return new LearningStats();

        } catch (Exception e) {
            log.error("获取学习统计异常", e);
            return new LearningStats();
        }
    }

    private List<LearningRecordVO> parseLearningRecordsFromResponse(String response) {
        try {
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    return objectMapper.convertValue(data, 
                            new TypeReference<List<LearningRecordVO>>() {});
                }
            }
        } catch (Exception e) {
            log.error("解析学习记录响应异常", e);
        }
        return Collections.emptyList();
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

