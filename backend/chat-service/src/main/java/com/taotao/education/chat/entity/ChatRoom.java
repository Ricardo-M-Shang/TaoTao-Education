package com.taotao.education.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天室实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chat_room")
public class ChatRoom extends BaseEntity {

    /**
     * 聊天室名称
     */
    private String name;

    /**
     * 聊天室描述
     */
    private String description;

    /**
     * 聊天室封面
     */
    private String cover;

    /**
     * 关联课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseTitle;

    /**
     * 创建者ID（讲师）
     */
    private Long creatorId;

    /**
     * 创建者名称
     */
    private String creatorName;

    /**
     * 创建者头像
     */
    private String creatorAvatar;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 最大成员数（0表示不限制）
     */
    private Integer maxMembers;

    /**
     * 状态 0-禁用 1-正常
     */
    private Integer status;

    /**
     * 是否需要审批加入 0-否 1-是
     */
    private Integer needApproval;

    /**
     * 公告
     */
    private String announcement;
}

