package com.taotao.education.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程学员VO（用于邀请时选择）
 */
@Data
@Schema(description = "课程学员信息")
public class CourseStudentVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "是否已在聊天室")
    private Boolean inRoom;

    @Schema(description = "是否已被邀请")
    private Boolean invited;
}

