package com.taotao.education.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "机构趋势指标")
public class OrgTrendVO {
    private LocalDate day;
    private BigDecimal income;
    private Integer paidOrders;
}


