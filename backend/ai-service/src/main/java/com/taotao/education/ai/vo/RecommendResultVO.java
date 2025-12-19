package com.taotao.education.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推荐结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推荐结果")
public class RecommendResultVO {

    @Schema(description = "推荐课程列表")
    private List<RecommendCourseVO> courses;

    @Schema(description = "推荐生成时间")
    private LocalDateTime generatedAt;

    @Schema(description = "是否来自缓存")
    private Boolean fromCache;

    @Schema(description = "推荐说明")
    private String summary;
}

