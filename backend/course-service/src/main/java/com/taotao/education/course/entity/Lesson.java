package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课时实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_lesson")
public class Lesson extends BaseEntity {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 课时标题
     */
    private String title;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 视频时长（秒）
     */
    private Long duration;

    /**
     * 是否可试看 0-否 1-是
     */
    private Integer isFree;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 课时类型 1-视频 2-图文 3-直播
     */
    private Integer type;
}

