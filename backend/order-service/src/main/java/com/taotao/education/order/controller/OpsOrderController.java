package com.taotao.education.order.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.order.service.OrderService;
import com.taotao.education.order.vo.OpsOrderOverviewVO;
import com.taotao.education.order.vo.OpsTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运营端订单/收入统计
 */
@Tag(name = "运营-订单统计", description = "运营端订单与收入看板接口")
@RestController
@RequestMapping("/api/ops/order")
@RequiredArgsConstructor
public class OpsOrderController {

    private final OrderService orderService;

    @Operation(summary = "运营端订单/收入概览")
    @GetMapping("/overview")
    public Result<OpsOrderOverviewVO> overview() {
        return Result.success(orderService.getOpsOverview());
    }

    @Operation(summary = "运营端收入/订单趋势")
    @GetMapping("/trend")
    public Result<List<OpsTrendVO>> trend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(orderService.getOpsTrend(days));
    }
}


