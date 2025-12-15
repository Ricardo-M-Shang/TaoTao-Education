package com.taotao.education.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天室VO
 */
@Data
@Schema(description = "聊天室信息")
public class ChatRoomVO {

    @Schema(description = "聊天室ID")
    private Long id;

    @Schema(description = "聊天室名称")
    private String name;

    @Schema(description = "聊天室描述")
    private String description;

    @Schema(description = "聊天室封面")
    private String cover;

    @Schema(description = "关联课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseTitle;

    @Schema(description = "创建者ID")
    private Long creatorId;

    @Schema(description = "创建者名称")
    private String creatorName;

    @Schema(description = "创建者头像")
    private String creatorAvatar;

    @Schema(description = "成员数量")
    private Integer memberCount;

    @Schema(description = "最大成员数")
    private Integer maxMembers;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "公告")
    private String announcement;

    @Schema(description = "是否需要审批加入")
    private Integer needApproval;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最新消息内容")
    private String latestMessage;

    @Schema(description = "最新消息时间")
    private LocalDateTime latestMessageTime;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "是否置顶")
    private Integer isPinned;

    @Schema(description = "当前用户是否是创建者")
    private Boolean isCreator;

    @Schema(description = "当前用户角色")
    private Integer myRole;
}

