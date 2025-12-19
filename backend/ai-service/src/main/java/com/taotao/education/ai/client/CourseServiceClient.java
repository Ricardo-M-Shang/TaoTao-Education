package com.taotao.education.ai.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.education.ai.config.ServiceConfig;
import com.taotao.education.ai.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

/**
 * 课程服务客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseServiceClient {

    private final WebClient webClient;
    private final ServiceConfig serviceConfig;
    private final ObjectMapper objectMapper;

    /**
     * 获取热门课程列表
     *
     * @param limit 数量限制
     * @return 课程列表
     */
    public List<CourseVO> getPopularCourses(int limit) {
        try {
            log.debug("获取热门课程列表: limit={}", limit);

            String response = webClient.get()
                    .uri(serviceConfig.getCourseUrl() + "/api/course/list?pageSize=" + limit + "&status=2")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseCoursesFromResponse(response);

        } catch (WebClientResponseException e) {
            log.error("获取热门课程失败: status={}", e.getStatusCode());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("获取热门课程异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 按分类获取课程
     *
     * @param categoryId 分类ID
     * @param limit      数量限制
     * @return 课程列表
     */
    public List<CourseVO> getCoursesByCategory(Long categoryId, int limit) {
        try {
            log.debug("按分类获取课程: categoryId={}, limit={}", categoryId, limit);

            String response = webClient.get()
                    .uri(serviceConfig.getCourseUrl() + "/api/course/list?categoryId=" + categoryId + "&pageSize=" + limit + "&status=2")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseCoursesFromResponse(response);

        } catch (WebClientResponseException e) {
            log.error("按分类获取课程失败: status={}", e.getStatusCode());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("按分类获取课程异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程信息
     */
    public CourseVO getCourseById(Long courseId) {
        try {
            log.debug("获取课程详情: courseId={}", courseId);

            String response = webClient.get()
                    .uri(serviceConfig.getCourseUrl() + "/api/course/" + courseId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.get("data");
                if (data != null && !data.isNull()) {
                    return objectMapper.treeToValue(data, CourseVO.class);
                }
            }
            return null;

        } catch (WebClientResponseException e) {
            log.error("获取课程详情失败: status={}", e.getStatusCode());
            return null;
        } catch (Exception e) {
            log.error("获取课程详情异常", e);
            return null;
        }
    }

    /**
     * 批量获取课程详情
     *
     * @param courseIds 课程ID列表
     * @return 课程列表
     */
    public List<CourseVO> getCoursesByIds(List<Long> courseIds) {
        return courseIds.stream()
                .map(this::getCourseById)
                .filter(course -> course != null)
                .toList();
    }

    /**
     * 获取所有分类
     *
     * @return 分类名称列表
     */
    public List<String> getAllCategories() {
        try {
            log.debug("获取所有分类");

            String response = webClient.get()
                    .uri(serviceConfig.getCourseUrl() + "/api/course/category/list")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    return objectMapper.convertValue(data, 
                            new TypeReference<List<JsonNode>>() {})
                            .stream()
                            .map(node -> node.get("name").asText())
                            .toList();
                }
            }
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("获取分类列表异常", e);
            return Collections.emptyList();
        }
    }

    private List<CourseVO> parseCoursesFromResponse(String response) {
        try {
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.get("data");
                if (data != null) {
                    // 处理分页响应
                    JsonNode records = data.get("records");
                    if (records != null && records.isArray()) {
                        return objectMapper.convertValue(records, 
                                new TypeReference<List<CourseVO>>() {});
                    }
                    // 处理列表响应
                    if (data.isArray()) {
                        return objectMapper.convertValue(data, 
                                new TypeReference<List<CourseVO>>() {});
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析课程响应异常", e);
        }
        return Collections.emptyList();
    }
}

