package com.taotao.education.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户课程VO
 */
@Data
@Schema(description = "用户课程信息")
public class UserCourseVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "课程封面")
    private String courseCover;

    @Schema(description = "讲师名称")
    private String teacherName;

    @Schema(description = "学习进度")
    private Integer progress;

    @Schema(description = "最后学习时间")
    private LocalDateTime lastStudyTime;

    @Schema(description = "是否完成")
    private Integer isFinished;

    @Schema(description = "购买时间")
    private LocalDateTime createTime;
}

