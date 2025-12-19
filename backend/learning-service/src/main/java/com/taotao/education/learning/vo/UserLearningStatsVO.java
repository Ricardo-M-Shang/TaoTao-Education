package com.taotao.education.learning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户学习总统计VO
 */
@Data
@Schema(description = "用户学习总统计")
public class UserLearningStatsVO {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "学习课程总数")
    private Integer totalCourses;
    
    @Schema(description = "已完成课程数")
    private Integer completedCourses;
    
    @Schema(description = "总学习时长(秒)")
    private Integer totalStudyDuration;
    
    @Schema(description = "总学习天数")
    private Integer totalStudyDays;
    
    @Schema(description = "日均学习时长(秒)")
    private Integer avgDailyDuration;
    
    @Schema(description = "最长连续学习天数")
    private Integer longestStreak;
    
    @Schema(description = "当前连续学习天数")
    private Integer currentStreak;
    
    @Schema(description = "最近学习日期")
    private String lastStudyDate;
    
    @Schema(description = "等级积分")
    private Integer levelScore;
    
    @Schema(description = "证书数量")
    private Integer certificatesCount;
}
