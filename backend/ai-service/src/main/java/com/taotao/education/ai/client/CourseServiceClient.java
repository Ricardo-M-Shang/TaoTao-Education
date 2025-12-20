package com.taotao.education.ai.client;

import com.taotao.education.ai.client.feign.CourseClient;
import com.taotao.education.ai.vo.CourseVO;
import com.taotao.education.common.result.PageResult;
import com.taotao.education.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 课程服务客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseServiceClient {

    private final CourseClient courseClient;

    /**
     * 获取热门课程列表
     *
     * @param limit 数量限制
     * @return 课程列表
     */
    public List<CourseVO> getPopularCourses(int limit) {
        try {
            log.debug("获取热门课程列表: limit={}", limit);

            Result<PageResult<CourseVO>> result = courseClient.getCourseList(limit, 2, null);
            
            if (result != null && result.getData() != null && result.getData().getRecords() != null) {
                return result.getData().getRecords();
            }

        } catch (Exception e) {
            log.error("获取热门课程异常", e);
        }
        return Collections.emptyList();
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

            Result<PageResult<CourseVO>> result = courseClient.getCourseList(limit, 2, categoryId);
            
            if (result != null && result.getData() != null && result.getData().getRecords() != null) {
                return result.getData().getRecords();
            }

        } catch (Exception e) {
            log.error("按分类获取课程异常", e);
        }
        return Collections.emptyList();
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

            Result<CourseVO> result = courseClient.getCourseById(courseId);
            
            if (result != null && result.getData() != null) {
                return result.getData();
            }

        } catch (Exception e) {
            log.error("获取课程详情异常", e);
        }
        return null;
    }

    /**
     * 批量获取课程详情
     *
     * @param courseIds 课程ID列表
     * @return 课程列表
     */
    public List<CourseVO> getCoursesByIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }
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

            Result<List<Map<String, Object>>> result = courseClient.getCategoryList();
            
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .map(map -> (String) map.get("name"))
                        .toList();
            }

        } catch (Exception e) {
            log.error("获取分类列表异常", e);
        }
        return Collections.emptyList();
    }
}
