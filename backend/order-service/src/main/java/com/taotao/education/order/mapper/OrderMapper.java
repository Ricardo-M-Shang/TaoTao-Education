package com.taotao.education.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 计算讲师已支付订单金额
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(pay_amount),0) FROM t_order WHERE teacher_id = #{teacherId} AND status = 1")
    java.math.BigDecimal sumPaidAmountByTeacher(Long teacherId);

    /**
     * 计算讲师今日收入
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(pay_amount),0) FROM t_order WHERE teacher_id = #{teacherId} AND status = 1 AND DATE(pay_time) = CURDATE()")
    java.math.BigDecimal sumTodayIncomeByTeacher(Long teacherId);

    /**
     * 计算讲师本月收入
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(pay_amount),0) FROM t_order WHERE teacher_id = #{teacherId} AND status = 1 AND DATE_FORMAT(pay_time,'%Y-%m') = DATE_FORMAT(CURDATE(),'%Y-%m')")
    java.math.BigDecimal sumMonthIncomeByTeacher(Long teacherId);

    /**
     * 统计讲师已支付订单数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(1) FROM t_order WHERE teacher_id = #{teacherId} AND status = 1")
    Integer countPaidOrdersByTeacher(Long teacherId);

    /**
     * 机构收入汇总
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(org_income),0) FROM t_order WHERE org_id = #{orgId} AND status = 1")
    java.math.BigDecimal sumPaidAmountByOrg(Long orgId);

    /**
     * 机构今日收入
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(org_income),0) FROM t_order WHERE org_id = #{orgId} AND status = 1 AND DATE(pay_time) = CURDATE()")
    java.math.BigDecimal sumTodayIncomeByOrg(Long orgId);

    /**
     * 机构本月收入
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(org_income),0) FROM t_order WHERE org_id = #{orgId} AND status = 1 AND DATE_FORMAT(pay_time,'%Y-%m') = DATE_FORMAT(CURDATE(),'%Y-%m')")
    java.math.BigDecimal sumMonthIncomeByOrg(Long orgId);

    /**
     * 机构已支付订单数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(1) FROM t_order WHERE org_id = #{orgId} AND status = 1")
    Integer countPaidOrdersByOrg(Long orgId);

    /**
     * 机构最近N天收入
     */
    @org.apache.ibatis.annotations.Select("""
            SELECT DATE(pay_time) AS day, COALESCE(SUM(org_income),0) AS income, COUNT(1) AS paidOrders
            FROM t_order
            WHERE org_id = #{orgId} AND status = 1 AND pay_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
            GROUP BY DATE(pay_time)
            ORDER BY day
            """)
    java.util.List<com.taotao.education.order.vo.OrgTrendVO> sumIncomeTrendByOrg(Long orgId, Integer days);

    /**
     * 平台总收入
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(pay_amount),0) FROM t_order WHERE status = 1")
    java.math.BigDecimal sumPaidAmountAll();

    /**
     * 平台今日收入
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(pay_amount),0) FROM t_order WHERE status = 1 AND DATE(pay_time) = CURDATE()")
    java.math.BigDecimal sumTodayIncomeAll();

    /**
     * 平台本月收入
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(pay_amount),0) FROM t_order WHERE status = 1 AND DATE_FORMAT(pay_time,'%Y-%m') = DATE_FORMAT(CURDATE(),'%Y-%m')")
    java.math.BigDecimal sumMonthIncomeAll();

    /**
     * 平台已支付订单数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(1) FROM t_order WHERE status = 1")
    Integer countPaidOrdersAll();

    /**
     * 平台最近N天收入与订单趋势
     */
    @org.apache.ibatis.annotations.Select("""
            SELECT DATE(pay_time) AS day, COALESCE(SUM(pay_amount),0) AS income, COUNT(1) AS paidOrders
            FROM t_order
            WHERE status = 1 AND pay_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
            GROUP BY DATE(pay_time)
            ORDER BY day
            """)
    java.util.List<com.taotao.education.order.vo.OpsTrendVO> sumIncomeTrendAll(Integer days);
}

