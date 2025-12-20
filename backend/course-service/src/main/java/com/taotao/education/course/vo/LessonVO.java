package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 课时VO
 */
@Data
@Schema(description = "课时信息")
public class LessonVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "课时ID")
    private Long id;

    @Schema(description = "课时标题")
    private String title;

    @Schema(description = "视频URL")
    private String videoUrl;

    @Schema(description = "视频时长（秒）")
    private Long duration;

    @Schema(description = "是否可试看")
    private Integer isFree;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "课时类型 1-视频 2-图文 3-直播")
    private Integer type;
}

