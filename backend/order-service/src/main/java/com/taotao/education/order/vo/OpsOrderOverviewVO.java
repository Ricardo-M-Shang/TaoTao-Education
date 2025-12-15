package com.taotao.education.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营端订单/收入概览
 */
@Data
public class OpsOrderOverviewVO {

    /**
     * 平台总收入
     */
    private BigDecimal totalIncome;

    /**
     * 今日收入
     */
    private BigDecimal todayIncome;

    /**
     * 本月收入
     */
    private BigDecimal monthIncome;

    /**
     * 已支付订单数
     */
    private Integer paidOrders;
}


