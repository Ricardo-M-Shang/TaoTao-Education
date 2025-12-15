package com.taotao.education.learning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程学习统计VO
 */
@Data
@Schema(description = "课程学习统计")
public class LearningStatsVO {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "课程封面")
    private String courseCover;

    @Schema(description = "讲师名称")
    private String teacherName;

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

    @Schema(description = "是否完成课程")
    private Boolean isFinished;

    @Schema(description = "学习状态 not_started-未开始 learning-学习中 completed-已完成")
    private String status;
}
