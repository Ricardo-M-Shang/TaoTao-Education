package com.taotao.education.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单VO
 */
@Data
@Schema(description = "订单信息")
public class OrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "课程封面")
    private String courseCover;

    @Schema(description = "讲师名称")
    private String teacherName;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付方式 1-支付宝 2-微信")
    private Integer payType;

    @Schema(description = "订单状态 0-待支付 1-已支付 2-已取消 3-已退款")
    private Integer status;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

