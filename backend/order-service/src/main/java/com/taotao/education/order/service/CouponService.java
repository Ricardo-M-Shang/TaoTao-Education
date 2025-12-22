package com.taotao.education.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.order.dto.CouponQueryDTO;
import com.taotao.education.order.dto.CouponReceiveDTO;
import com.taotao.education.order.entity.Coupon;
import com.taotao.education.order.vo.CouponVO;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService extends IService<Coupon> {

    void receive(Long userId, CouponReceiveDTO dto);

    /**
     * 秒杀领券（内部调用）
     */
    void receiveSeckill(Long userId, Long couponId);

    Page<CouponVO> listUserCoupons(Long userId, CouponQueryDTO queryDTO);

    /**
     * 获取所有秒杀优惠券
     */
    List<CouponVO> listSeckillCoupons();

    /**
     * 查询用户可用优惠券（按课程金额过滤）
     */
    List<CouponVO> listAvailable(Long userId, BigDecimal orderAmount);

    /**
     * 校验并计算优惠，返回优惠金额
     */
    BigDecimal verifyAndCalc(Long userId, Long couponId, BigDecimal originalPrice);
}


