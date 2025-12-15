package com.taotao.education.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学习进度DTO
 */
@Data
@Schema(description = "学习进度参数")
public class StudyProgressDTO {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Long courseId;

    @NotNull(message = "课时ID不能为空")
    @Schema(description = "课时ID")
    private Long lessonId;

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "学习时长（秒）")
    private Integer duration;

    @Schema(description = "课时进度（百分比）")
    private Integer progress;
}

