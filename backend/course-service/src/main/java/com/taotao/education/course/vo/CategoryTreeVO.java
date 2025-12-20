package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分类树VO
 */
@Data
@Schema(description = "分类树")
public class CategoryTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类ID")
    private Long parentId;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "子分类")
    private List<CategoryTreeVO> children;
}

