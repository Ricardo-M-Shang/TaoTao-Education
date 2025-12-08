package com.taotao.education.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新课程DTO
 */
@Data
@Schema(description = "更新课程请求")
public class CourseUpdateDTO {

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程副标题")
    private String subtitle;

    @Schema(description = "课程封面")
    private String cover;

    @Schema(description = "课程简介")
    private String description;

    @Schema(description = "课程详情")
    private String content;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "课程类型 1-录播 2-直播 3-图文")
    private Integer type;

    @Schema(description = "课程价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "是否免费 0-收费 1-免费")
    private Integer isFree;
}

