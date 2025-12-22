package com.taotao.education.order.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.order.entity.Coupon;
import com.taotao.education.order.service.SeckillService;
import com.taotao.education.order.vo.CouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "秒杀管理")
@RestController
@RequestMapping("/api/order/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "获取秒杀列表")
    @GetMapping("/list")
    public Result<List<CouponVO>> list() {
        return Result.success(seckillService.listSeckillCoupons());
    }

    @Operation(summary = "秒杀优惠券")
    @PostMapping("/{couponId}")
    public Result<String> seckill(@RequestHeader("X-User-Id") Long userId, @PathVariable Long couponId) {
        return seckillService.doSeckill(userId, couponId);
    }
    
    @Operation(summary = "预热库存(测试用)")
    @PostMapping("/preheat")
    public Result<Void> preheat(@RequestBody Coupon coupon) {
        seckillService.preheat(coupon);
        return Result.success();
    }
}
