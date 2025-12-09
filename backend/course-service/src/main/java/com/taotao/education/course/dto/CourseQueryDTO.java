package com.taotao.education.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程查询DTO
 */
@Data
@Schema(description = "课程查询请求")
public class CourseQueryDTO {

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "课程类型 1-录播 2-直播 3-图文")
    private Integer type;

    @Schema(description = "是否免费 0-收费 1-免费")
    private Integer isFree;

    @Schema(description = "课程状态 0-草稿 2-已发布 3-已下架（讲师端使用）")
    private Integer status;

    @Schema(description = "排序方式 popular-最热 newest-最新 price-价格")
    private String orderBy;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;
}

