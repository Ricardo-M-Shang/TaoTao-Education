package com.taotao.education.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应VO
 */
@Data
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色 1-学员 2-讲师 3-机构管理员 4-运营管理员")
    private Integer role;
}

