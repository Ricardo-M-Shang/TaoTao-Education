package com.taotao.education.ai.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习记录VO（从learning-service获取）
 */
@Data
public class LearningRecordVO {

    private Long id;

    private Long userId;

    private Long courseId;

    private String courseTitle;

    private Long lessonId;

    private String lessonTitle;

    private Integer progress;

    private Long studyDuration;

    private LocalDateTime lastStudyTime;
}

