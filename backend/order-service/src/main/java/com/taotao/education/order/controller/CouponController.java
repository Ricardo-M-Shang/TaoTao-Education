package com.taotao.education.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.order.dto.CouponQueryDTO;
import com.taotao.education.order.dto.CouponReceiveDTO;
import com.taotao.education.order.entity.Coupon;
import com.taotao.education.order.service.CouponService;
import com.taotao.education.order.vo.CouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "优惠券", description = "优惠券领取、查询接口")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // --- User Side APIs ---

    @Operation(summary = "领取优惠券")
    @PostMapping("/coupon/receive")
    public Result<Void> receive(@RequestHeader("X-User-Id") Long userId,
                                @Valid @RequestBody CouponReceiveDTO dto) {
        couponService.receive(userId, dto);
        return Result.success();
    }

    @Operation(summary = "我的优惠券列表")
    @GetMapping("/coupon/my")
    public Result<Page<CouponVO>> myCoupons(@RequestHeader("X-User-Id") Long userId,
                                            CouponQueryDTO queryDTO) {
        return Result.success(couponService.listUserCoupons(userId, queryDTO));
    }

    @Operation(summary = "可用优惠券（按订单金额过滤）")
    @GetMapping("/coupon/available")
    public Result<List<CouponVO>> available(@RequestHeader("X-User-Id") Long userId,
                                            @RequestParam BigDecimal orderAmount) {
        return Result.success(couponService.listAvailable(userId, orderAmount));
    }

    // --- Ops Side APIs ---

    @Operation(summary = "运营端-优惠券列表")
    @GetMapping("/ops/coupon/list")
    public Result<Page<Coupon>> opsList(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(required = false) Integer type) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) wrapper.like(Coupon::getName, name);
        if (status != null) wrapper.eq(Coupon::getStatus, status);
        if (type != null) wrapper.eq(Coupon::getType, type);
        wrapper.orderByDesc(Coupon::getId);
        return Result.success(couponService.page(page, wrapper));
    }

    @Operation(summary = "运营端-创建优惠券")
    @PostMapping("/ops/coupon/create")
    public Result<Void> opsCreate(@RequestBody Coupon coupon) {
        coupon.setStock(coupon.getTotal()); // initial stock
        couponService.save(coupon);
        return Result.success();
    }

    @Operation(summary = "运营端-更新状态")
    @PutMapping("/ops/coupon/{id}/status")
    public Result<Void> opsUpdateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Coupon c = new Coupon();
        c.setId(id);
        c.setStatus(status);
        couponService.updateById(c);
        return Result.success();
    }

    @Operation(summary = "运营端-删除优惠券")
    @DeleteMapping("/ops/coupon/{id}")
    public Result<Void> opsDelete(@PathVariable Long id) {
        couponService.removeById(id);
        return Result.success();
    }
}


