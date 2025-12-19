package com.taotao.education.ai.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程信息VO（从course-service获取）
 */
@Data
public class CourseVO {

    private Long id;

    private String title;

    private String subtitle;

    private String cover;

    private String description;

    private String teacherName;

    private String categoryName;

    private Long categoryId;

    private Integer type;

    private BigDecimal price;

    private Integer isFree;

    private Integer lessonCount;

    private Integer studyCount;

    private BigDecimal score;

    private Long totalDuration;
}

