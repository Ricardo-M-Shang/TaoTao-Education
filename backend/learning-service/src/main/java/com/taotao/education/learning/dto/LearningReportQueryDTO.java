package com.taotao.education.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学习报告查询DTO
 */
@Data
@Schema(description = "学习报告查询参数")
public class LearningReportQueryDTO {

    @Schema(description = "时间周期 week-周 month-月 year-年", example = "week")
    private String period = "week";

    @Schema(description = "课程ID（可选，查询特定课程）")
    private Long courseId;

    @Schema(description = "报告类型 summary-概要 detail-详细", example = "summary")
    private String type = "summary";
}
