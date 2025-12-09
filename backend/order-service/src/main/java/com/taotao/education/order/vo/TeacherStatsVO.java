package com.taotao.education.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 讲师收益统计VO
 */
@Data
public class TeacherStatsVO {

    /**
     * 累计收益
     */
    private BigDecimal totalIncome;

    /**
     * 今日收益
     */
    private BigDecimal todayIncome;

    /**
     * 本月收益
     */
    private BigDecimal monthIncome;

    /**
     * 已支付订单数
     */
    private Integer paidOrders;

    /**
     * 学员人数
     */
    private Integer studentCount;
}

