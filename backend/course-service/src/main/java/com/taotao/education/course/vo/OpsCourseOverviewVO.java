package com.taotao.education.course.vo;

import lombok.Data;

/**
 * 运营端课程概览
 */
@Data
public class OpsCourseOverviewVO {

    /**
     * 课程总数
     */
    private Long total;

    /**
     * 待审核课程数
     */
    private Long pending;

    /**
     * 已发布课程数
     */
    private Long published;

    /**
     * 已下架课程数
     */
    private Long offline;
}


