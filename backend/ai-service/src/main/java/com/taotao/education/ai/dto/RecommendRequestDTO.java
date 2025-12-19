package com.taotao.education.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 推荐请求DTO
 */
@Data
@Schema(description = "推荐请求参数")
public class RecommendRequestDTO {

    @Schema(description = "推荐数量", example = "5")
    private Integer count;

    @Schema(description = "是否包含已学习课程", example = "false")
    private Boolean includeLearnedCourses;

    @Schema(description = "指定分类ID")
    private Long categoryId;
}

