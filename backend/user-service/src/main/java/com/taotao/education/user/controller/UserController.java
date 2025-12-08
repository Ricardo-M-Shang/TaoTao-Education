package com.taotao.education.user.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.user.dto.LoginDTO;
import com.taotao.education.user.dto.PasswordChangeDTO;
import com.taotao.education.user.dto.RegisterDTO;
import com.taotao.education.user.dto.UserUpdateDTO;
import com.taotao.education.user.service.UserService;
import com.taotao.education.user.vo.LoginVO;
import com.taotao.education.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestHeader("X-User-Id") Long userId) {
        UserVO userVO = userService.getCurrentUser(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody UserUpdateDTO updateDTO) {
        userService.updateUserInfo(userId, updateDTO);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password/change")
    public Result<Void> changePassword(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(userId, passwordChangeDTO.getOldPassword(), passwordChangeDTO.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-User-Id") Long userId) {
        userService.logout(userId);
        return Result.success();
    }
}

