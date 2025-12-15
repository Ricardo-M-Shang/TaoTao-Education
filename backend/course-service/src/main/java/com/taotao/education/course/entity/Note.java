package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学习笔记实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_note")
public class Note extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课时ID
     */
    private Long lessonId;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 视频时间点（秒）
     */
    private Integer videoTime;
}

