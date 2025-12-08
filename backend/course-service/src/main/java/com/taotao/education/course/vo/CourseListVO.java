package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程列表VO
 */
@Data
@Schema(description = "课程列表项")
public class CourseListVO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程副标题")
    private String subtitle;

    @Schema(description = "课程封面")
    private String cover;

    @Schema(description = "讲师名称")
    private String teacherName;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "课程类型 1-录播 2-直播 3-图文")
    private Integer type;

    @Schema(description = "课程价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "是否免费")
    private Integer isFree;

    @Schema(description = "课时数")
    private Integer lessonCount;

    @Schema(description = "学习人数")
    private Integer studyCount;

    @Schema(description = "评分")
    private BigDecimal score;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

