package com.taotao.education.order.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.order.service.OrderService;
import com.taotao.education.order.vo.OrgTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "机构趋势", description = "机构收入/订单趋势接口")
@RestController
@RequestMapping("/api/order/org")
@RequiredArgsConstructor
public class OrgTrendController {

    private final OrderService orderService;

    @Operation(summary = "机构收入/订单趋势")
    @GetMapping("/trend")
    public Result<List<OrgTrendVO>> trend(@RequestHeader("X-User-Id") Long orgId,
                                          @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(orderService.getOrgTrend(orgId, days));
    }
}


