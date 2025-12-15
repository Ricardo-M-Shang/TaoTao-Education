package com.taotao.education.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.learning.entity.LearningStats;
import com.taotao.education.learning.vo.LearningStatsVO;

import java.util.List;

/**
 * 学习统计服务接口
 */
public interface LearningStatsService extends IService<LearningStats> {

    /**
     * 获取用户课程学习统计
     */
    LearningStatsVO getCourseStats(Long userId, Long courseId);

    /**
     * 获取用户所有课程学习统计
     */
    List<LearningStatsVO> getUserCourseStats(Long userId);

    /**
     * 获取用户正在学习的课程
     */
    List<LearningStatsVO> getLearningCourses(Long userId);

    /**
     * 获取用户已完成的课程
     */
    List<LearningStatsVO> getCompletedCourses(Long userId);

    /**
     * 更新课程学习统计
     */
    void updateCourseStats(Long userId, Long courseId);

    /**
     * 更新用户所有课程学习统计
     */
    void updateUserCourseStats(Long userId);

    /**
     * 获取课程完成率分布
     */
    List<java.util.Map<String, Object>> getCompletionRateDistribution(Long userId);
}
