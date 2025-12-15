package com.taotao.education.user.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.user.service.UserService;
import com.taotao.education.user.vo.OpsUserOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营端用户统计接口
 */
@Tag(name = "运营-用户统计", description = "运营端用户概览接口")
@RestController
@RequestMapping("/api/user/ops")
@RequiredArgsConstructor
public class OpsUserController {

    private final UserService userService;

    @Operation(summary = "用户概览")
    @GetMapping("/overview")
    public Result<OpsUserOverviewVO> overview() {
        return Result.success(userService.getOpsOverview());
    }
}


