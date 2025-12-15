package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习记录VO
 */
@Data
@Schema(description = "学习记录")
public class StudyRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课时ID")
    private Long lessonId;

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "学习时长（秒）")
    private Integer duration;

    @Schema(description = "课时进度")
    private Integer progress;

    @Schema(description = "是否完成")
    private Integer isFinished;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

