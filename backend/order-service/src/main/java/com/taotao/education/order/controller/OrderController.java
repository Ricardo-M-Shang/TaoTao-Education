package com.taotao.education.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.order.dto.OrderCreateDTO;
import com.taotao.education.order.dto.PayDTO;
import com.taotao.education.order.service.OrderService;
import com.taotao.education.order.service.UserCourseService;
import com.taotao.education.order.vo.TeacherStatsVO;
import com.taotao.education.order.vo.TeacherStudentVO;
import com.taotao.education.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "订单创建、支付、查询等接口")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserCourseService userCourseService;

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<String> createOrder(@RequestHeader("X-User-Id") Long userId,
                                      @Valid @RequestBody OrderCreateDTO createDTO) {
        String orderNo = orderService.createOrder(userId, createDTO);
        return Result.success(orderNo);
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/detail/{orderNo}")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNo) {
        OrderVO orderVO = orderService.getOrderDetail(orderNo);
        return Result.success(orderVO);
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping("/list")
    public Result<Page<OrderVO>> getUserOrders(@RequestHeader("X-User-Id") Long userId,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<OrderVO> page = orderService.getUserOrders(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancelOrder(@RequestHeader("X-User-Id") Long userId,
                                    @PathVariable String orderNo) {
        orderService.cancelOrder(userId, orderNo);
        return Result.success();
    }

    @Operation(summary = "支付订单（模拟支付）")
    @PostMapping("/pay")
    public Result<Void> payOrder(@Valid @RequestBody PayDTO payDTO) {
        orderService.payOrder(payDTO.getOrderNo(), payDTO.getPayType());
        return Result.success();
    }

    @Operation(summary = "检查是否已购买课程")
    @GetMapping("/check/{courseId}")
    public Result<Boolean> checkBuy(@RequestHeader("X-User-Id") Long userId,
                                    @PathVariable Long courseId) {
        boolean bought = orderService.checkUserBuyCourse(userId, courseId);
        return Result.success(bought);
    }

    @Operation(summary = "讲师收益统计")
    @GetMapping("/teacher/stats")
    public Result<TeacherStatsVO> teacherStats(@RequestHeader("X-User-Id") Long teacherId) {
        TeacherStatsVO stats = orderService.getTeacherStats(teacherId);
        return Result.success(stats);
    }

    @Operation(summary = "讲师查看课程学员列表")
    @GetMapping("/teacher/students/{courseId}")
    public Result<List<TeacherStudentVO>> listStudents(@RequestHeader("X-User-Id") Long teacherId,
                                                       @PathVariable Long courseId) {
        List<TeacherStudentVO> students = userCourseService.listStudentsByCourse(teacherId, courseId);
        return Result.success(students);
    }
}

