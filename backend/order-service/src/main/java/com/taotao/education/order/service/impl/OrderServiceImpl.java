package com.taotao.education.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.common.result.ResultCode;
import com.taotao.education.order.dto.OrderCreateDTO;
import com.taotao.education.order.entity.Order;
import com.taotao.education.order.entity.UserCourse;
import com.taotao.education.order.entity.UserCoupon;
import com.taotao.education.order.mapper.OrderMapper;
import com.taotao.education.order.mapper.UserCourseMapper;
import com.taotao.education.order.mapper.UserCouponMapper;
import com.taotao.education.order.service.OrderService;
import com.taotao.education.order.service.CouponService;
import com.taotao.education.order.vo.OrderVO;
import com.taotao.education.order.vo.OrgStatsVO;
import com.taotao.education.order.vo.OpsOrderOverviewVO;
import com.taotao.education.order.vo.OpsTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final UserCourseMapper userCourseMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponService couponService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderCreateDTO createDTO) {
        // 检查是否已购买
        if (checkUserBuyCourse(userId, createDTO.getCourseId())) {
            throw new BusinessException("您已购买过该课程");
        }

        // 生成订单号
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        BigDecimal discount = couponService.verifyAndCalc(userId, createDTO.getCouponId(), createDTO.getOriginalPrice());

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTeacherId(createDTO.getTeacherId());
        order.setOrgId(createDTO.getOrgId());
        order.setOrgName(createDTO.getOrgName());
        order.setCourseId(createDTO.getCourseId());
        order.setCourseTitle(createDTO.getCourseTitle());
        order.setCourseCover(createDTO.getCourseCover());
        order.setTeacherName(createDTO.getTeacherName());
        order.setUsername(createDTO.getUsername());
        order.setOriginalPrice(createDTO.getOriginalPrice());
        order.setCouponId(createDTO.getCouponId());
        order.setDiscountAmount(discount);
        order.setPayAmount(createDTO.getOriginalPrice().subtract(discount));
        order.setStatus(0); // 待支付
        order.setExpireTime(LocalDateTime.now().plusMinutes(30)); // 30分钟后过期

        this.save(order);
        return orderNo;
    }

    @Override
    public OrderVO getOrderDetail(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = this.getOne(wrapper);

        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public Page<OrderVO> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> result = this.page(page, wrapper);

        Page<OrderVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<OrderVO> voList = result.getRecords().stream()
                .map(order -> {
                    OrderVO vo = new OrderVO();
                    BeanUtils.copyProperties(order, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public void cancelOrder(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getUserId, userId);
        Order order = this.getOne(wrapper);

        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许取消");
        }

        order.setStatus(2); // 已取消
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo, Integer payType) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = this.getOne(wrapper);

        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_PAID);
        }

        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.ORDER_EXPIRED);
        }

        // 更新订单状态
        order.setStatus(1); // 已支付
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        BigDecimal payAmount = order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount();
        BigDecimal orgIncome = payAmount.multiply(new BigDecimal("0.6")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformIncome = payAmount.subtract(orgIncome).setScale(2, RoundingMode.HALF_UP);
        order.setOrgIncome(orgIncome);
        order.setPlatformIncome(platformIncome);
        this.updateById(order);

        // 核销优惠券
        if (order.getCouponId() != null) {
            LambdaQueryWrapper<UserCoupon> ucWrapper = new LambdaQueryWrapper<>();
            ucWrapper.eq(UserCoupon::getId, order.getCouponId())
                    .eq(UserCoupon::getUserId, order.getUserId())
                    .eq(UserCoupon::getStatus, 0);
            UserCoupon uc = userCouponMapper.selectOne(ucWrapper);
            if (uc != null) {
                uc.setStatus(1);
                uc.setOrderNo(orderNo);
                uc.setUseTime(LocalDateTime.now());
                userCouponMapper.updateById(uc);
            }
        }

        // 创建用户课程关联
        UserCourse userCourse = new UserCourse();
        userCourse.setUserId(order.getUserId());
        userCourse.setCourseId(order.getCourseId());
        userCourse.setOrderId(order.getId());
        userCourse.setProgress(0);
        userCourse.setIsFinished(0);
        userCourseMapper.insert(userCourse);
    }

    @Override
    public boolean checkUserBuyCourse(Long userId, Long courseId) {
        LambdaQueryWrapper<UserCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCourse::getUserId, userId)
               .eq(UserCourse::getCourseId, courseId);
        return userCourseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public com.taotao.education.order.vo.TeacherStatsVO getTeacherStats(Long teacherId) {
        com.taotao.education.order.vo.TeacherStatsVO vo = new com.taotao.education.order.vo.TeacherStatsVO();
        vo.setTotalIncome(baseMapper.sumPaidAmountByTeacher(teacherId));
        vo.setTodayIncome(baseMapper.sumTodayIncomeByTeacher(teacherId));
        vo.setMonthIncome(baseMapper.sumMonthIncomeByTeacher(teacherId));
        vo.setPaidOrders(baseMapper.countPaidOrdersByTeacher(teacherId));

        // 学员数按已支付订单的去重用户
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getTeacherId, teacherId)
               .eq(Order::getStatus, 1)
               .select(Order::getUserId)
               .groupBy(Order::getUserId);
        vo.setStudentCount((int) this.count(wrapper));
        return vo;
    }

    @Override
    public Page<OrderVO> getOrgOrders(Long orgId, Integer status, Long teacherId, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrgId, orgId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (teacherId != null) {
            wrapper.eq(Order::getTeacherId, teacherId);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = this.page(page, wrapper);
        Page<OrderVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<OrderVO> voList = result.getRecords().stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public OrgStatsVO getOrgStats(Long orgId) {
        OrgStatsVO vo = new OrgStatsVO();
        vo.setTotalIncome(baseMapper.sumPaidAmountByOrg(orgId));
        vo.setTodayIncome(baseMapper.sumTodayIncomeByOrg(orgId));
        vo.setMonthIncome(baseMapper.sumMonthIncomeByOrg(orgId));
        vo.setPaidOrders(baseMapper.countPaidOrdersByOrg(orgId));
        return vo;
    }

    @Override
    public java.util.List<com.taotao.education.order.vo.OrgTrendVO> getOrgTrend(Long orgId, Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        return baseMapper.sumIncomeTrendByOrg(orgId, days);
    }

    @Override
    public OpsOrderOverviewVO getOpsOverview() {
        OpsOrderOverviewVO vo = new OpsOrderOverviewVO();
        vo.setTotalIncome(baseMapper.sumPaidAmountAll());
        vo.setTodayIncome(baseMapper.sumTodayIncomeAll());
        vo.setMonthIncome(baseMapper.sumMonthIncomeAll());
        vo.setPaidOrders(baseMapper.countPaidOrdersAll());
        return vo;
    }

    @Override
    public List<OpsTrendVO> getOpsTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        return baseMapper.sumIncomeTrendAll(days);
    }
}

