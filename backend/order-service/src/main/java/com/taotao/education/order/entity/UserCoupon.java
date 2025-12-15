package com.taotao.education.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_coupon")
public class UserCoupon extends BaseEntity {

    private Long couponId;
    private Long userId;
    /**
     * 0-未使用 1-已使用 2-已过期
     */
    private Integer status;
    private String orderNo;
    private LocalDateTime obtainTime;
    private LocalDateTime useTime;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}


