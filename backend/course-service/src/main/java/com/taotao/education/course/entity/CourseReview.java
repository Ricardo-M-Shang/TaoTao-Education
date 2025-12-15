package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程评价实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_course_review")
public class CourseReview extends BaseEntity {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 评分（1-5星）
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 是否匿名 0-否 1-是
     */
    private Integer isAnonymous;
}

