package com.taotao.education.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.common.result.ResultCode;
import com.taotao.education.common.result.Result;
import com.taotao.education.order.client.feign.CourseClient;
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
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.seata.spring.annotation.GlobalTransactional;
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
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final UserCourseMapper userCourseMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponService couponService;
    private final CourseClient courseClient;

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
    @GlobalTransactional(name = "order-create-global-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String createOrderWithGlobalTx(Long userId, OrderCreateDTO createDTO, boolean simulateFailure) {
        log.info("Seata全局事务开始: xid={}, userId={}, courseId={}, simulateFailure={}",
                RootContext.getXID(), userId, createDTO.getCourseId(), simulateFailure);
        String orderNo = createOrder(userId, createDTO);
        Result<Void> remoteResult = courseClient.increaseStudyCount(createDTO.getCourseId(), 1);
        if (remoteResult == null || remoteResult.getCode() == null || remoteResult.getCode() != 200) {
            throw new BusinessException(remoteResult == null ? "课程服务调用失败" : remoteResult.getMessage());
        }
        if (simulateFailure) {
            throw new BusinessException("模拟异常：触发Seata全局事务回滚");
        }
        return orderNo;
    }

    @Override
    @SentinelResource(value = "order:query:detail", blockHandler = "getOrderDetailBlockHandler")
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

    public OrderVO getOrderDetailBlockHandler(String orderNo, BlockException ex) {
        throw new BusinessException("系统繁忙，请稍后重试");
    }

    @Override
    @SentinelResource(value = "order:query:user-orders", blockHandler = "getUserOrdersBlockHandler")
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

    public Page<OrderVO> getUserOrdersBlockHandler(Long userId, Integer status, Integer pageNum, Integer pageSize, BlockException ex) {
        throw new BusinessException("系统繁忙，请稍后重试");
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
        BigDecimal teacherIncome = payAmount.multiply(new BigDecimal("0.3")).setScale(2, RoundingMode.HALF_UP); // 讲师分成30% (逻辑计算)
        BigDecimal platformIncome = payAmount.subtract(orgIncome).subtract(teacherIncome).setScale(2, RoundingMode.HALF_UP);
        
        order.setOrgIncome(orgIncome);
        // order.setTeacherIncome(teacherIncome); // 暂不持久化到数据库
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
        vo.setTotalIncome(baseMapper.sumTotalIncomeByTeacher(teacherId));
        vo.setTodayIncome(baseMapper.sumTodayIncomeByTeacher(teacherId));
        vo.setMonthIncome(baseMapper.sumMonthIncomeByTeacher(teacherId));
        vo.setPaidOrders(baseMapper.countPaidOrdersByTeacher(teacherId));

        // 学员数按已支付订单的去重用户
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getTeacherId, teacherId)
               .eq(Order::getStatus, 1)
               .select(Order::getUserId)
               .groupBy(Order::getUserId);
        
        // 使用 list 而不是 count，因为 count 在 groupBy 下行为可能不符合预期
        // 或者直接查询 count(distinct user_id)
        // 这里采用 MyBatis Plus 的 count(wrapper) 会生成 select count(1) from (select ... group by ...)
        // 如果返回多行，count() 方法可能会抛异常或者返回总行数，这里 selectOne 报错说明 MybatisPlus 内部处理 group by count 有问题
        
        // 修正方案：直接用 list 查询出来 size，或者手写 SQL
        // 简单修正：查询所有符合条件的记录数（如果不考虑 group by 性能），或者手写 Mapper
        
        // 更好的修正：使用 mapper 自定义查询 count(distinct user_id)
        vo.setStudentCount(baseMapper.countStudentsByTeacher(teacherId));
        return vo;
    }

    @Override
    @SentinelResource(value = "order:query:org-orders", blockHandler = "getOrgOrdersBlockHandler")
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

    public Page<OrderVO> getOrgOrdersBlockHandler(Long orgId, Integer status, Long teacherId, Integer pageNum, Integer pageSize, BlockException ex) {
        throw new BusinessException("系统繁忙，请稍后重试");
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

