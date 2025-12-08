package com.taotao.education.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建课程DTO
 */
@Data
@Schema(description = "创建课程请求")
public class CourseCreateDTO {

    @Schema(description = "课程标题", required = true)
    @NotBlank(message = "课程标题不能为空")
    private String title;

    @Schema(description = "课程副标题")
    private String subtitle;

    @Schema(description = "课程封面")
    private String cover;

    @Schema(description = "课程简介")
    private String description;

    @Schema(description = "课程详情")
    private String content;

    @Schema(description = "分类ID", required = true)
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Schema(description = "课程类型 1-录播 2-直播 3-图文", required = true)
    @NotNull(message = "课程类型不能为空")
    private Integer type;

    @Schema(description = "课程价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "是否免费 0-收费 1-免费")
    private Integer isFree;
}

