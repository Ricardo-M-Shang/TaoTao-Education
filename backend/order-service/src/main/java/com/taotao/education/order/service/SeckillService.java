package com.taotao.education.order.service;

import com.taotao.education.common.result.Result;
import com.taotao.education.order.entity.Coupon;
import com.taotao.education.order.vo.CouponVO;

import java.util.List;

public interface SeckillService {
    /**
     * 执行秒杀
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 结果
     */
    Result<String> doSeckill(Long userId, Long couponId);

    /**
     * 预热库存
     * @param coupon 优惠券
     */
    void preheat(Coupon coupon);

    /**
     * 获取秒杀列表
     */
    List<CouponVO> listSeckillCoupons();
}
