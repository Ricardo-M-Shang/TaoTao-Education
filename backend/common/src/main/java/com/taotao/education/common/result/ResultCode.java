package com.taotao.education.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    
    // 认证相关 1xxx
    UNAUTHORIZED(1001, "未登录或token已过期"),
    FORBIDDEN(1002, "没有相关权限"),
    TOKEN_INVALID(1003, "token无效"),
    TOKEN_EXPIRED(1004, "token已过期"),
    
    // 参数相关 2xxx
    PARAM_ERROR(2001, "参数错误"),
    PARAM_MISSING(2002, "参数缺失"),
    PARAM_INVALID(2003, "参数无效"),
    
    // 用户相关 3xxx
    USER_NOT_FOUND(3001, "用户不存在"),
    USER_EXISTS(3002, "用户已存在"),
    PASSWORD_ERROR(3003, "密码错误"),
    ACCOUNT_DISABLED(3004, "账号已被禁用"),
    
    // 课程相关 4xxx
    COURSE_NOT_FOUND(4001, "课程不存在"),
    COURSE_OFFLINE(4002, "课程已下架"),
    COURSE_NOT_PURCHASED(4003, "课程未购买"),
    
    // 订单相关 5xxx
    ORDER_NOT_FOUND(5001, "订单不存在"),
    ORDER_EXPIRED(5002, "订单已过期"),
    ORDER_PAID(5003, "订单已支付"),
    PAY_FAIL(5004, "支付失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

