package com.taotao.education.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "机构收益统计")
public class OrgStatsVO {

    @Schema(description = "总收入")
    private BigDecimal totalIncome;

    @Schema(description = "今日收入")
    private BigDecimal todayIncome;

    @Schema(description = "本月收入")
    private BigDecimal monthIncome;

    @Schema(description = "已支付订单数")
    private Integer paidOrders;
}


