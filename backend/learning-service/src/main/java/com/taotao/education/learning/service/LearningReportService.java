package com.taotao.education.learning.service;

import com.taotao.education.learning.dto.LearningReportQueryDTO;
import com.taotao.education.learning.vo.LearningReportVO;

import java.util.List;
import java.util.Map;

/**
 * 学习报告服务接口
 */
public interface LearningReportService {

    /**
     * 生成学习报告
     */
    LearningReportVO generateLearningReport(Long userId, LearningReportQueryDTO queryDTO);

    /**
     * 获取学习时长趋势
     */
    List<Map<String, Object>> getStudyDurationTrend(Long userId, String period);

    /**
     * 获取学习时段分布
     */
    List<Map<String, Object>> getStudyTimeDistribution(Long userId, String period);

    /**
     * 获取学习建议
     */
    List<String> getLearningAdvice(Long userId);

    /**
     * 计算用户学习等级
     */
    Integer calculateUserLevel(Long userId);
}
