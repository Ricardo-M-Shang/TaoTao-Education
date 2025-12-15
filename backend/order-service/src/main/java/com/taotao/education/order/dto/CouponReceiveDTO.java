package com.taotao.education.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "领取优惠券请求")
public class CouponReceiveDTO {

    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;
}


