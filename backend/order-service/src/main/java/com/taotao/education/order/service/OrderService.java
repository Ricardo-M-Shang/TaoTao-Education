package com.taotao.education.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.order.dto.OrderCreateDTO;
import com.taotao.education.order.entity.Order;
import com.taotao.education.order.vo.OrderVO;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    String createOrder(Long userId, OrderCreateDTO createDTO);

    /**
     * Seata全局事务创建订单（跨服务）
     */
    String createOrderWithGlobalTx(Long userId, OrderCreateDTO createDTO, boolean simulateFailure);

    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(String orderNo);

    /**
     * 分页查询用户订单
     */
    Page<OrderVO> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 取消订单
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 支付订单（模拟支付）
     */
    void payOrder(String orderNo, Integer payType);

    /**
     * 检查用户是否购买过课程
     */
    boolean checkUserBuyCourse(Long userId, Long courseId);

    /**
     * 讲师收益统计
     */
    com.taotao.education.order.vo.TeacherStatsVO getTeacherStats(Long teacherId);

    /**
     * 机构订单列表
     */
    Page<OrderVO> getOrgOrders(Long orgId, Integer status, Long teacherId, Integer pageNum, Integer pageSize);

    /**
     * 机构收益统计
     */
    com.taotao.education.order.vo.OrgStatsVO getOrgStats(Long orgId);

    /**
     * 机构收入/订单趋势
     */
    java.util.List<com.taotao.education.order.vo.OrgTrendVO> getOrgTrend(Long orgId, Integer days);

    /**
     * 运营端订单/收入概览
     */
    com.taotao.education.order.vo.OpsOrderOverviewVO getOpsOverview();

    /**
     * 运营端收入/订单趋势
     */
    java.util.List<com.taotao.education.order.vo.OpsTrendVO> getOpsTrend(Integer days);
}

