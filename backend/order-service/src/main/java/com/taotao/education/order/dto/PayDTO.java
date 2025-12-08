package com.taotao.education.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 支付DTO
 */
@Data
@Schema(description = "支付请求")
public class PayDTO {

    @Schema(description = "订单号", required = true)
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @Schema(description = "支付方式 1-支付宝 2-微信", required = true)
    @NotNull(message = "支付方式不能为空")
    private Integer payType;
}

