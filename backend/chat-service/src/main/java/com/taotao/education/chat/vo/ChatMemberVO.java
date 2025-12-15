package com.taotao.education.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天室成员VO
 */
@Data
@Schema(description = "聊天室成员")
public class ChatMemberVO {

    @Schema(description = "成员记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "角色 1-普通成员 2-管理员 3-创建者")
    private Integer role;

    @Schema(description = "状态 0-已退出 1-正常 2-已禁言")
    private Integer status;

    @Schema(description = "加入时间")
    private LocalDateTime joinTime;

    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    @Schema(description = "是否在线")
    private Boolean isOnline;
}

