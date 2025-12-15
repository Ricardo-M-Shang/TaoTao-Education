package com.taotao.education.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学习进度记录DTO
 */
@Data
@Schema(description = "学习进度记录")
public class StudyProgressDTO {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Long courseId;

    @NotNull(message = "课时ID不能为空")
    @Schema(description = "课时ID")
    private Long lessonId;

    @Schema(description = "本次学习时长(秒)")
    private Integer studyDuration;

    @Schema(description = "视频总时长(秒)")
    private Integer videoDuration;

    @NotNull(message = "最后播放位置不能为空")
    @Schema(description = "最后播放位置(秒)")
    private Integer lastPosition;

    @Schema(description = "是否完成 0-未完成 1-已完成")
    private Integer isCompleted;

    @Schema(description = "设备类型")
    private String deviceType;
}
