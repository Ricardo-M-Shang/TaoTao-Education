package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程问答-回答
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_course_answer")
public class CourseAnswer extends BaseEntity {

    private Long questionId;

    private Long courseId;

    private Long userId;

    private String username;

    private String nickname;

    private String content;

    /**
     * 是否被采纳 0-否 1-是
     */
    private Integer accepted;
}


