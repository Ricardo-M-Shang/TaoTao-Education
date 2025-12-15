package com.taotao.education.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天室邀请实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chat_invitation")
public class ChatInvitation extends BaseEntity {

    /**
     * 聊天室ID
     */
    private Long roomId;

    /**
     * 聊天室名称
     */
    private String roomName;

    /**
     * 被邀请用户ID
     */
    private Long inviteeId;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 邀请人名称
     */
    private String inviterName;

    /**
     * 邀请人头像
     */
    private String inviterAvatar;

    /**
     * 状态 0-待处理 1-已接受 2-已拒绝 3-已过期
     */
    private Integer status;

    /**
     * 邀请消息
     */
    private String message;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
}

