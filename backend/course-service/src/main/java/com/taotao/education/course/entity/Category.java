package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_category")
public class Category extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID，0表示一级分类
     */
    private Long parentId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String icon;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;
}

