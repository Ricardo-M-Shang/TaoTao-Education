package com.taotao.education.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 推荐课程VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推荐课程信息")
public class RecommendCourseVO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程副标题")
    private String subtitle;

    @Schema(description = "课程封面")
    private String cover;

    @Schema(description = "课程简介")
    private String description;

    @Schema(description = "讲师名称")
    private String teacherName;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "课程价格")
    private BigDecimal price;

    @Schema(description = "是否免费")
    private Integer isFree;

    @Schema(description = "学习人数")
    private Integer studyCount;

    @Schema(description = "评分")
    private BigDecimal score;

    @Schema(description = "AI推荐理由")
    private String reason;

    @Schema(description = "推荐得分")
    private Double recommendScore;

    /**
     * 从 CourseVO 构建
     */
    public static RecommendCourseVO fromCourseVO(CourseVO course, String reason, Double score) {
        return RecommendCourseVO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .cover(course.getCover())
                .description(course.getDescription())
                .teacherName(course.getTeacherName())
                .categoryName(course.getCategoryName())
                .price(course.getPrice())
                .isFree(course.getIsFree())
                .studyCount(course.getStudyCount())
                .score(course.getScore())
                .reason(reason)
                .recommendScore(score)
                .build();
    }
}

