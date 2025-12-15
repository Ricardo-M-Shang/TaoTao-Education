package com.taotao.education.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    /**
     * 讲师ID
     */
    private Long teacherId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;


    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程标题
     */
    private String courseTitle;

    /**
     * 课程封面
     */
    private String courseCover;

    /**
     * 讲师名称
     */
    private String teacherName;

    /**
     * 机构ID
     */
    private Long orgId;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 订单原价
     */
    private BigDecimal originalPrice;

    /**
     * 使用的优惠券ID
     */
    private Long couponId;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 机构分成
     */
    private BigDecimal orgIncome;

    /**
     * 平台分成
     */
    private BigDecimal platformIncome;

    /**
     * 支付方式 1-支付宝 2-微信
     */
    private Integer payType;

    /**
     * 订单状态 0-待支付 1-已支付 2-已取消 3-已退款
     */
    private Integer status;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 备注
     */
    private String remark;
}

