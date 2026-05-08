package com.taotao.education.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息更新DTO
 */
@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateDTO {

    @Schema(description = "昵称")
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱")
    @Pattern(regexp = "^$|^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式不正确")
    private String email;

    @Schema(description = "性别 0-未知 1-男 2-女")
    private Integer gender;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "个性签名")
    @Size(max = 100, message = "签名长度不能超过100个字符")
    private String signature;
}

