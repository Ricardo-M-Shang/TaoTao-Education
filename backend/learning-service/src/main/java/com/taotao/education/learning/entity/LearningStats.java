package com.taotao.education.learning.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程学习统计实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_learning_stats")
@Schema(description = "课程学习统计")
public class LearningStats extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程总课时数")
    private Integer totalLessons;

    @Schema(description = "已完成课时数")
    private Integer completedLessons;

    @Schema(description = "课程总时长(秒)")
    private Integer totalDuration;

    @Schema(description = "已学习时长(秒)")
    private Integer studyDuration;

    @Schema(description = "完成率")
    private Double completionRate;

    @Schema(description = "首次学习时间")
    private LocalDateTime firstStudyTime;

    @Schema(description = "最后学习时间")
    private LocalDateTime lastStudyTime;

    @Schema(description = "学习天数")
    private Integer studyDays;

    @Schema(description = "日均学习时长(秒)")
    private Integer avgDailyDuration;

    @Schema(description = "是否完成课程 0-未完成 1-已完成")
    private Integer isFinished;

    @Schema(description = "证书ID")
    private Long certificateId;
}
