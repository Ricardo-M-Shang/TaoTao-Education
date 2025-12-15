package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程问答-问题
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_course_question")
public class CourseQuestion extends BaseEntity {

    private Long courseId;

    private Long userId;

    private String username;

    private String nickname;

    private String content;
}


