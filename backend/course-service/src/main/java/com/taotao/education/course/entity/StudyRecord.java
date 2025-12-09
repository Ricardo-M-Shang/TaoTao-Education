package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学习记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_study_record")
public class StudyRecord extends BaseEntity {

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
     * 章节ID
     */
    private Long chapterId;

    /**
     * 本次学习时长（秒）
     */
    private Integer duration;

    /**
     * 课时学习进度（百分比）
     */
    private Integer progress;

    /**
     * 是否完成 0-未完成 1-已完成
     */
    private Integer isFinished;
}

