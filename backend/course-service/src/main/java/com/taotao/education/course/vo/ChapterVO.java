package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 章节VO
 */
@Data
@Schema(description = "章节信息")
public class ChapterVO {

    @Schema(description = "章节ID")
    private Long id;

    @Schema(description = "章节标题")
    private String title;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "课时列表")
    private List<LessonVO> lessons;
}

