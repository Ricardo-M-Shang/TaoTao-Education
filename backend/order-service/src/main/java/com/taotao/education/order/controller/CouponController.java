package com.taotao.education.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.order.dto.CouponQueryDTO;
import com.taotao.education.order.dto.CouponReceiveDTO;
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
@RequestMapping("/api/order/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "领取优惠券")
    @PostMapping("/receive")
    public Result<Void> receive(@RequestHeader("X-User-Id") Long userId,
                                @Valid @RequestBody CouponReceiveDTO dto) {
        couponService.receive(userId, dto);
        return Result.success();
    }

    @Operation(summary = "我的优惠券列表")
    @GetMapping("/my")
    public Result<Page<CouponVO>> myCoupons(@RequestHeader("X-User-Id") Long userId,
                                            CouponQueryDTO queryDTO) {
        return Result.success(couponService.listUserCoupons(userId, queryDTO));
    }

    @Operation(summary = "可用优惠券（按订单金额过滤）")
    @GetMapping("/available")
    public Result<List<CouponVO>> available(@RequestHeader("X-User-Id") Long userId,
                                            @RequestParam BigDecimal orderAmount) {
        return Result.success(couponService.listAvailable(userId, orderAmount));
    }
}


