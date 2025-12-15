package com.taotao.education.learning.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户学习总统计实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_learning_stats")
@Schema(description = "用户学习总统计")
public class UserLearningStats extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "总学习课程数")
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

    @Schema(description = "最后学习日期")
    private LocalDate lastStudyDate;

    @Schema(description = "学习等级积分")
    private Integer levelScore;

    @Schema(description = "获得证书数量")
    private Integer certificatesCount;
}
