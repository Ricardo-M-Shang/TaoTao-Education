package com.taotao.education.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价创建DTO
 */
@Data
@Schema(description = "评价创建参数")
public class ReviewCreateDTO {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Long courseId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    @Schema(description = "评分（1-5星）")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "是否匿名 0-否 1-是")
    private Integer isAnonymous = 0;
}

