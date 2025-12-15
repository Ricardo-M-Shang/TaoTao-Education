package com.taotao.education.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营端收入与订单趋势
 */
@Data
public class OpsTrendVO {

    private String day;

    private BigDecimal income;

    private Integer paidOrders;
}


