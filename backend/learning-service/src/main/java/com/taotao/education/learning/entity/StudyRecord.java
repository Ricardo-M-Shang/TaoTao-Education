package com.taotao.education.learning.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学习记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_learning_record")
@Schema(description = "学习记录")
public class StudyRecord extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课时ID")
    private Long lessonId;

    @Schema(description = "本次学习时长(秒)")
    private Integer studyDuration;

    @Schema(description = "视频总时长(秒)")
    private Integer videoDuration;

    @Schema(description = "最后播放位置(秒)")
    private Integer lastPosition;

    @Schema(description = "播放进度百分比")
    private Double progressPercent;

    @Schema(description = "是否完成 0-未完成 1-已完成")
    private Integer isCompleted;

    @Schema(description = "学习日期")
    private LocalDate studyDate;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "IP地址")
    private String ipAddress;
}
