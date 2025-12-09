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
}

