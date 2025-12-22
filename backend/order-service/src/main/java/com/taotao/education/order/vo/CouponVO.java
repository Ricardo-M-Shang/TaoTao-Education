package com.taotao.education.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "优惠券VO")
public class CouponVO {
    private Long id;
    private String name;
    private Integer type; // 1满减 2折扣
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private BigDecimal thresholdAmount;
    private Integer status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer total;
    private Integer stock;
    private Integer limitPerUser;
    
    @Schema(description = "抢购开始时间")
    private LocalDateTime grabStartTime;
    @Schema(description = "抢购结束时间")
    private LocalDateTime grabEndTime;

    // 用户券状态
    private Long userCouponId;
    private Integer userStatus;
    private LocalDateTime obtainTime;
    private LocalDateTime useTime;
}


