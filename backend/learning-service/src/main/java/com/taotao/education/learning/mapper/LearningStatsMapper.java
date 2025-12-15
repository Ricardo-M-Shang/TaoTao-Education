package com.taotao.education.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.learning.entity.LearningStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程学习统计Mapper
 */
@Mapper
public interface LearningStatsMapper extends BaseMapper<LearningStats> {

    /**
     * 获取用户所有课程学习统计
     */
    @Select("SELECT * FROM t_learning_stats " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY last_study_time DESC")
    List<LearningStats> getUserCourseStats(@Param("userId") Long userId);

    /**
     * 获取用户已完成的课程统计
     */
    @Select("SELECT * FROM t_learning_stats " +
            "WHERE user_id = #{userId} AND is_finished = 1 AND deleted = 0 " +
            "ORDER BY update_time DESC")
    List<LearningStats> getCompletedCourses(@Param("userId") Long userId);

    /**
     * 获取用户正在学习的课程统计
     */
    @Select("SELECT * FROM t_learning_stats " +
            "WHERE user_id = #{userId} AND is_finished = 0 AND completion_rate > 0 AND deleted = 0 " +
            "ORDER BY last_study_time DESC")
    List<LearningStats> getLearningCourses(@Param("userId") Long userId);

    /**
     * 统计用户课程完成率分布
     */
    @Select("SELECT " +
            "CASE " +
            "WHEN completion_rate = 0 THEN '未开始' " +
            "WHEN completion_rate > 0 AND completion_rate < 25 THEN '0-25%' " +
            "WHEN completion_rate >= 25 AND completion_rate < 50 THEN '25-50%' " +
            "WHEN completion_rate >= 50 AND completion_rate < 75 THEN '50-75%' " +
            "WHEN completion_rate >= 75 AND completion_rate < 100 THEN '75-99%' " +
            "WHEN completion_rate = 100 THEN '已完成' " +
            "END as range_name, " +
            "COUNT(*) as count " +
            "FROM t_learning_stats " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "GROUP BY range_name " +
            "ORDER BY MIN(completion_rate)")
    List<java.util.Map<String, Object>> getCompletionRateDistribution(@Param("userId") Long userId);
}
