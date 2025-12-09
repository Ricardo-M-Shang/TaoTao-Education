package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程收藏VO
 */
@Data
@Schema(description = "课程收藏")
public class CourseFavoriteVO {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "课程封面")
    private String courseCover;

    @Schema(description = "讲师名称")
    private String teacherName;

    @Schema(description = "课程价格")
    private BigDecimal price;

    @Schema(description = "是否免费")
    private Integer isFree;

    @Schema(description = "学习人数")
    private Integer studyCount;

    @Schema(description = "评分")
    private BigDecimal score;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}

