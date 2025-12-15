package com.taotao.education.learning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 学习报告VO
 */
@Data
@Schema(description = "学习报告")
public class LearningReportVO {

    @Schema(description = "报告时间段")
    private String period;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    // ========== 基础统计 ==========
    @Schema(description = "总学习时长(秒)")
    private Integer totalStudyDuration;

    @Schema(description = "总学习天数")
    private Integer totalStudyDays;

    @Schema(description = "学习课程数")
    private Integer studyCourseCount;

    @Schema(description = "完成课程数")
    private Integer completedCourseCount;

    @Schema(description = "日均学习时长(秒)")
    private Integer avgDailyDuration;

    @Schema(description = "当前连续学习天数")
    private Integer currentStreak;

    // ========== 趋势数据 ==========
    @Schema(description = "学习时长趋势")
    private List<Map<String, Object>> durationTrend;

    @Schema(description = "学习进度趋势")
    private List<Map<String, Object>> progressTrend;

    // ========== 分布数据 ==========
    @Schema(description = "课程完成率分布")
    private List<Map<String, Object>> completionDistribution;

    @Schema(description = "学习时段分布")
    private List<Map<String, Object>> timeDistribution;

    // ========== 课程数据 ==========
    @Schema(description = "最近学习的课程")
    private List<LearningStatsVO> recentCourses;

    @Schema(description = "最活跃的课程（本周期学习时长最多）")
    private List<LearningStatsVO> activeCourses;

    // ========== 成就数据 ==========
    @Schema(description = "本周期新完成的课程")
    private List<LearningStatsVO> newCompletedCourses;

    @Schema(description = "学习等级")
    private Integer level;

    @Schema(description = "距离下一等级还需积分")
    private Integer nextLevelPoints;

    @Schema(description = "学习建议")
    private List<String> suggestions;
}
