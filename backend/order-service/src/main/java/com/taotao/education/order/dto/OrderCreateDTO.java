package com.taotao.education.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单DTO
 */
@Data
@Schema(description = "创建订单请求")
public class OrderCreateDTO {

    @Schema(description = "课程ID", required = true)
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "课程封面")
    private String courseCover;

    @Schema(description = "讲师名称")
    private String teacherName;

    @Schema(description = "原价")
    private BigDecimal originalPrice;
}

