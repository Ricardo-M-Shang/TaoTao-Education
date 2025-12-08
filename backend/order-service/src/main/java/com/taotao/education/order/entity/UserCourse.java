package com.taotao.education.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户课程关联实体类（用户已购买的课程）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_course")
public class UserCourse extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 学习进度（百分比）
     */
    private Integer progress;

    /**
     * 最后学习时间
     */
    private LocalDateTime lastStudyTime;

    /**
     * 当前学习的课时ID
     */
    private Long currentLessonId;

    /**
     * 是否完成 0-未完成 1-已完成
     */
    private Integer isFinished;
}

