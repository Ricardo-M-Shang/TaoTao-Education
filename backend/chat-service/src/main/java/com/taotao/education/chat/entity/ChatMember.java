package com.taotao.education.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天室成员实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chat_member")
public class ChatMember extends BaseEntity {

    /**
     * 聊天室ID
     */
    private Long roomId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 角色 1-普通成员 2-管理员 3-创建者
     */
    private Integer role;

    /**
     * 状态 0-已退出 1-正常 2-已禁言
     */
    private Integer status;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶 0-否 1-是
     */
    private Integer isPinned;

    /**
     * 是否免打扰 0-否 1-是
     */
    private Integer isMuted;
}

