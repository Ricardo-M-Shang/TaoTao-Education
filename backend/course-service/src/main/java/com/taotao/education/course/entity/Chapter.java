package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程章节实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chapter")
public class Chapter extends BaseEntity {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 排序
     */
    private Integer sort;
}

