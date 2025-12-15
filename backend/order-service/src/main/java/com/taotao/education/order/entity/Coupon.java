package com.taotao.education.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_coupon")
public class Coupon extends BaseEntity {

    private String name;
    /**
     * 1-满减 2-折扣
     */
    private Integer type;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private BigDecimal thresholdAmount;
    private Integer total;
    private Integer stock;
    private Integer limitPerUser;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    /**
     * 1-启用 0-禁用
     */
    private Integer status;
}


