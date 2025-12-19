package com.taotao.education.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.education.ai.client.CourseServiceClient;
import com.taotao.education.ai.client.DeepSeekClient;
import com.taotao.education.ai.client.LearningServiceClient;
import com.taotao.education.ai.config.RecommendConfig;
import com.taotao.education.ai.dto.ChatMessage;
import com.taotao.education.ai.dto.RecommendRequestDTO;
import com.taotao.education.ai.service.RecommendService;
import com.taotao.education.ai.vo.CourseVO;
import com.taotao.education.ai.vo.LearningRecordVO;
import com.taotao.education.ai.vo.RecommendCourseVO;
import com.taotao.education.ai.vo.RecommendResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 课程推荐服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final DeepSeekClient deepSeekClient;
    private final CourseServiceClient courseServiceClient;
    private final LearningServiceClient learningServiceClient;
    private final StringRedisTemplate redisTemplate;
    private final RecommendConfig recommendConfig;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "ai:recommend:user:";
    private static final String POPULAR_CACHE_KEY = "ai:recommend:popular";

    @Override
    public RecommendResultVO getRecommendations(Long userId, RecommendRequestDTO request) {
        int count = getValidCount(request);
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + count;

        // 尝试从缓存获取
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cached)) {
                log.debug("从缓存获取推荐结果: userId={}", userId);
                RecommendResultVO result = objectMapper.readValue(cached, RecommendResultVO.class);
                result.setFromCache(true);
                return result;
            }
        } catch (Exception e) {
            log.warn("读取推荐缓存失败", e);
        }

        // 生成新的推荐
        RecommendResultVO result = generateRecommendations(userId, request);
        result.setFromCache(false);

        // 缓存结果
        cacheResult(cacheKey, result);

        return result;
    }

    @Override
    public RecommendResultVO refreshRecommendations(Long userId, RecommendRequestDTO request) {
        int count = getValidCount(request);
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + count;

        // 删除缓存
        redisTemplate.delete(cacheKey);
        log.debug("已清除推荐缓存: userId={}", userId);

        // 重新生成
        RecommendResultVO result = generateRecommendations(userId, request);
        result.setFromCache(false);

        // 缓存结果
        cacheResult(cacheKey, result);

        return result;
    }

    @Override
    public RecommendResultVO getPopularRecommendations(int count) {
        // 尝试从缓存获取
        try {
            String cached = redisTemplate.opsForValue().get(POPULAR_CACHE_KEY);
            if (StringUtils.hasText(cached)) {
                log.debug("从缓存获取热门推荐");
                RecommendResultVO result = objectMapper.readValue(cached, RecommendResultVO.class);
                result.setFromCache(true);
                return result;
            }
        } catch (Exception e) {
            log.warn("读取热门推荐缓存失败", e);
        }

        // 获取热门课程
        List<CourseVO> popularCourses = courseServiceClient.getPopularCourses(count);
        List<RecommendCourseVO> recommendations = popularCourses.stream()
                .map(course -> RecommendCourseVO.fromCourseVO(course, "热门推荐课程", 0.9))
                .collect(Collectors.toList());

        RecommendResultVO result = RecommendResultVO.builder()
                .courses(recommendations)
                .generatedAt(LocalDateTime.now())
                .fromCache(false)
                .summary("为您推荐平台热门课程")
                .build();

        // 缓存结果
        cacheResult(POPULAR_CACHE_KEY, result);

        return result;
    }

    /**
     * 生成个性化推荐
     */
    private RecommendResultVO generateRecommendations(Long userId, RecommendRequestDTO request) {
        int count = getValidCount(request);
        boolean includeLearnedCourses = request != null && Boolean.TRUE.equals(request.getIncludeLearnedCourses());

        // 1. 获取用户学习历史
        List<LearningRecordVO> recentRecords = learningServiceClient.getRecentLearningRecords(userId, 20);
        Set<Long> learnedCourseIds = learningServiceClient.getLearnedCourseIds(userId);

        // 2. 获取候选课程
        List<CourseVO> candidateCourses = courseServiceClient.getPopularCourses(50);
        
        // 过滤已学习课程
        if (!includeLearnedCourses) {
            candidateCourses = candidateCourses.stream()
                    .filter(course -> !learnedCourseIds.contains(course.getId()))
                    .collect(Collectors.toList());
        }

        if (candidateCourses.isEmpty()) {
            return buildEmptyResult("暂无可推荐的课程");
        }

        // 3. 如果用户没有学习历史，返回热门推荐
        if (recentRecords.isEmpty() && learnedCourseIds.isEmpty()) {
            return buildFallbackResult(candidateCourses, count, "您还没有学习记录，为您推荐热门课程");
        }

        // 4. 构建 AI 推荐请求
        try {
            List<RecommendCourseVO> aiRecommendations = getAIRecommendations(
                    recentRecords, learnedCourseIds, candidateCourses, count);

            if (aiRecommendations.isEmpty()) {
                return buildFallbackResult(candidateCourses, count, "根据您的学习情况为您推荐");
            }

            return RecommendResultVO.builder()
                    .courses(aiRecommendations)
                    .generatedAt(LocalDateTime.now())
                    .fromCache(false)
                    .summary("根据您的学习历史，AI为您个性化推荐以下课程")
                    .build();

        } catch (Exception e) {
            log.error("AI推荐失败，使用降级策略", e);
            return buildFallbackResult(candidateCourses, count, "为您推荐以下课程");
        }
    }

    /**
     * 调用 AI 获取推荐
     */
    private List<RecommendCourseVO> getAIRecommendations(
            List<LearningRecordVO> recentRecords,
            Set<Long> learnedCourseIds,
            List<CourseVO> candidateCourses,
            int count) {

        // 构建用户学习历史描述
        StringBuilder userHistoryBuilder = new StringBuilder();
        if (!recentRecords.isEmpty()) {
            userHistoryBuilder.append("最近学习的课程：\n");
            recentRecords.stream().limit(10).forEach(record -> {
                userHistoryBuilder.append(String.format("- %s\n", record.getCourseTitle()));
            });
        }
        userHistoryBuilder.append(String.format("已学习课程数量：%d\n", learnedCourseIds.size()));

        // 构建候选课程列表
        StringBuilder coursesBuilder = new StringBuilder();
        coursesBuilder.append("候选课程列表：\n");
        candidateCourses.stream().limit(30).forEach(course -> {
            coursesBuilder.append(String.format(
                    "- ID:%d | 标题:%s | 分类:%s | 学习人数:%d | 评分:%s\n",
                    course.getId(),
                    course.getTitle(),
                    course.getCategoryName(),
                    course.getStudyCount() != null ? course.getStudyCount() : 0,
                    course.getScore() != null ? course.getScore().toString() : "暂无"
            ));
        });

        // 构建 Prompt
        String systemPrompt = """
                你是一个在线教育平台的课程推荐助手。你需要根据用户的学习历史，从候选课程中推荐最适合用户的课程。
                
                推荐原则：
                1. 推荐与用户已学课程相关或互补的课程
                2. 考虑课程的评分和学习人数
                3. 推荐能帮助用户进阶学习的课程
                4. 每个推荐都要有具体的理由
                
                请返回 JSON 格式，不要包含其他文字：
                [
                  {"id": 课程ID, "reason": "推荐理由", "score": 推荐得分(0.0-1.0)}
                ]
                """;

        String userMessage = String.format("""
                %s
                
                %s
                
                请推荐 %d 门最适合该用户的课程。
                """, userHistoryBuilder, coursesBuilder, count);

        // 调用 AI
        String response = deepSeekClient.chat(List.of(
                ChatMessage.system(systemPrompt),
                ChatMessage.user(userMessage)
        ));

        if (!StringUtils.hasText(response)) {
            return Collections.emptyList();
        }

        // 解析 AI 响应
        return parseAIResponse(response, candidateCourses);
    }

    /**
     * 解析 AI 响应
     */
    private List<RecommendCourseVO> parseAIResponse(String response, List<CourseVO> candidateCourses) {
        try {
            // 提取 JSON 部分
            String jsonPart = extractJson(response);
            if (jsonPart == null) {
                log.warn("无法从AI响应中提取JSON: {}", response);
                return Collections.emptyList();
            }

            List<Map<String, Object>> recommendations = objectMapper.readValue(
                    jsonPart, new TypeReference<List<Map<String, Object>>>() {});

            // 构建课程ID到课程的映射
            Map<Long, CourseVO> courseMap = candidateCourses.stream()
                    .collect(Collectors.toMap(CourseVO::getId, c -> c, (a, b) -> a));

            List<RecommendCourseVO> result = new ArrayList<>();
            for (Map<String, Object> rec : recommendations) {
                Object idObj = rec.get("id");
                Long courseId = idObj instanceof Number ? ((Number) idObj).longValue() : null;

                if (courseId != null && courseMap.containsKey(courseId)) {
                    CourseVO course = courseMap.get(courseId);
                    String reason = (String) rec.getOrDefault("reason", "AI推荐");
                    Double score = rec.get("score") instanceof Number 
                            ? ((Number) rec.get("score")).doubleValue() : 0.8;

                    result.add(RecommendCourseVO.fromCourseVO(course, reason, score));
                }
            }

            return result;

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", response, e);
            return Collections.emptyList();
        }
    }

    /**
     * 从响应中提取 JSON 数组
     */
    private String extractJson(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return null;
    }

    /**
     * 构建降级推荐结果
     */
    private RecommendResultVO buildFallbackResult(List<CourseVO> courses, int count, String summary) {
        List<RecommendCourseVO> recommendations = courses.stream()
                .limit(count)
                .map(course -> RecommendCourseVO.fromCourseVO(course, "热门推荐", 0.8))
                .collect(Collectors.toList());

        return RecommendResultVO.builder()
                .courses(recommendations)
                .generatedAt(LocalDateTime.now())
                .fromCache(false)
                .summary(summary)
                .build();
    }

    /**
     * 构建空结果
     */
    private RecommendResultVO buildEmptyResult(String summary) {
        return RecommendResultVO.builder()
                .courses(Collections.emptyList())
                .generatedAt(LocalDateTime.now())
                .fromCache(false)
                .summary(summary)
                .build();
    }

    /**
     * 缓存推荐结果
     */
    private void cacheResult(String key, RecommendResultVO result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, json, 
                    recommendConfig.getCacheTtlMinutes(), TimeUnit.MINUTES);
            log.debug("推荐结果已缓存: key={}", key);
        } catch (Exception e) {
            log.warn("缓存推荐结果失败", e);
        }
    }

    /**
     * 获取有效的推荐数量
     */
    private int getValidCount(RecommendRequestDTO request) {
        if (request == null || request.getCount() == null) {
            return recommendConfig.getDefaultCount();
        }
        int count = request.getCount();
        if (count < 1) {
            return recommendConfig.getDefaultCount();
        }
        if (count > recommendConfig.getMaxCount()) {
            return recommendConfig.getMaxCount();
        }
        return count;
    }
}

