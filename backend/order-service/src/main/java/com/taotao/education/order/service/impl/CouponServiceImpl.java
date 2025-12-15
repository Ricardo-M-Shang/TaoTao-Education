package com.taotao.education.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.order.dto.CouponQueryDTO;
import com.taotao.education.order.dto.CouponReceiveDTO;
import com.taotao.education.order.entity.Coupon;
import com.taotao.education.order.entity.UserCoupon;
import com.taotao.education.order.mapper.CouponMapper;
import com.taotao.education.order.mapper.UserCouponMapper;
import com.taotao.education.order.service.CouponService;
import com.taotao.education.order.vo.CouponVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final UserCouponMapper userCouponMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long userId, CouponReceiveDTO dto) {
        Coupon coupon = this.getById(dto.getCouponId());
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或未启用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidTo())) {
            throw new BusinessException("优惠券不在有效期内");
        }
        // 已领取校验
        LambdaQueryWrapper<UserCoupon> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserCoupon::getCouponId, dto.getCouponId())
                .eq(UserCoupon::getUserId, userId);
        if (userCouponMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("已领取该优惠券");
        }

        // 乐观扣减库存
        LambdaUpdateWrapper<Coupon> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Coupon::getId, dto.getCouponId())
                .gt(Coupon::getStock, 0)
                .setSql("stock = stock - 1");
        int updated = this.baseMapper.update(null, updateWrapper);
        if (updated == 0) {
            throw new BusinessException("优惠券已领完");
        }

        // 记录用户券
        UserCoupon uc = new UserCoupon();
        uc.setCouponId(dto.getCouponId());
        uc.setUserId(userId);
        uc.setStatus(0);
        uc.setOrderNo(null);
        uc.setObtainTime(now);
        uc.setValidFrom(coupon.getValidFrom());
        uc.setValidTo(coupon.getValidTo());
        userCouponMapper.insert(uc);
    }

    @Override
    public Page<CouponVO> listUserCoupons(Long userId, CouponQueryDTO queryDTO) {
        Page<UserCoupon> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        if (queryDTO.getStatus() != null) {
            wrapper.eq(UserCoupon::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(UserCoupon::getObtainTime);
        Page<UserCoupon> result = userCouponMapper.selectPage(page, wrapper);

        List<Long> couponIds = result.getRecords().stream().map(UserCoupon::getCouponId).toList();
        List<Coupon> coupons = couponIds.isEmpty() ? List.of() : this.listByIds(couponIds);
        // map
        List<CouponVO> voList = result.getRecords().stream().map(uc -> {
            CouponVO vo = new CouponVO();
            Coupon c = coupons.stream().filter(x -> x.getId().equals(uc.getCouponId())).findFirst().orElse(null);
            if (c != null) {
                BeanUtils.copyProperties(c, vo);
            }
            vo.setUserCouponId(uc.getId());
            vo.setUserStatus(uc.getStatus());
            vo.setObtainTime(uc.getObtainTime());
            vo.setUseTime(uc.getUseTime());
            return vo;
        }).collect(Collectors.toList());

        Page<CouponVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<CouponVO> listAvailable(Long userId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0)
                .gt(UserCoupon::getValidTo, now)
                .le(UserCoupon::getValidFrom, now);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);
        if (userCoupons.isEmpty()) {
            return List.of();
        }
        List<Long> couponIds = userCoupons.stream().map(UserCoupon::getCouponId).toList();
        List<Coupon> coupons = this.listByIds(couponIds);
        return userCoupons.stream().map(uc -> {
            CouponVO vo = new CouponVO();
            Coupon c = coupons.stream().filter(x -> x.getId().equals(uc.getCouponId())).findFirst().orElse(null);
            if (c != null) {
                BeanUtils.copyProperties(c, vo);
            }
            vo.setUserCouponId(uc.getId());
            vo.setUserStatus(uc.getStatus());
            vo.setObtainTime(uc.getObtainTime());
            vo.setUseTime(uc.getUseTime());
            // 门槛过滤
            if (c != null && c.getThresholdAmount() != null && orderAmount.compareTo(c.getThresholdAmount()) < 0) {
                return null; // 不满足门槛条件的直接过滤掉
            }
            return vo;
        }).filter(vo -> vo != null && (vo.getStatus() == null || vo.getStatus() == 1)) // 只返回可用的优惠券
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal verifyAndCalc(Long userId, Long couponId, BigDecimal originalPrice) {
        if (couponId == null) {
            return BigDecimal.ZERO;
        }
        UserCoupon uc = userCouponMapper.selectById(couponId);
        if (uc == null || !uc.getUserId().equals(userId) || uc.getStatus() != 0) {
            throw new BusinessException("优惠券不可用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(uc.getValidFrom()) || now.isAfter(uc.getValidTo())) {
            throw new BusinessException("优惠券已过期");
        }
        Coupon coupon = this.getById(uc.getCouponId());
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或未启用");
        }
        if (coupon.getThresholdAmount() != null && originalPrice.compareTo(coupon.getThresholdAmount()) < 0) {
            throw new BusinessException("未达到使用门槛");
        }
        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getType() != null && coupon.getType() == 1) {
            discount = coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        } else if (coupon.getType() != null && coupon.getType() == 2) {
            BigDecimal rate = coupon.getDiscountRate() == null ? BigDecimal.ONE : coupon.getDiscountRate();
            discount = originalPrice.subtract(originalPrice.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        }
        if (discount.compareTo(originalPrice) > 0) {
            discount = originalPrice;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}


