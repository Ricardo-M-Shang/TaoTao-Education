package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程收藏实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_course_favorite")
public class CourseFavorite extends BaseEntity {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 用户ID
     */
    private Long userId;
}

