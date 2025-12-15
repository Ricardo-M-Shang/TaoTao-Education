package com.taotao.education.learning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
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

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "课时ID")
    private Long lessonId;

    @Schema(description = "课时标题")
    private String lessonTitle;

    @Schema(description = "本次学习时长(秒)")
    private Integer studyDuration;

    @Schema(description = "视频总时长(秒)")
    private Integer videoDuration;

    @Schema(description = "最后播放位置(秒)")
    private Integer lastPosition;

    @Schema(description = "播放进度百分比")
    private Double progressPercent;

    @Schema(description = "是否完成")
    private Boolean isCompleted;

    @Schema(description = "学习日期")
    private LocalDate studyDate;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
