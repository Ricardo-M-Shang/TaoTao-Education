package com.taotao.education.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taotao.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_course")
public class Course extends BaseEntity {

    /**
     * 课程标题
     */
    private String title;

    /**
     * 课程副标题
     */
    private String subtitle;

    /**
     * 课程封面图片
     */
    private String cover;

    /**
     * 课程简介
     */
    private String description;

    /**
     * 课程详情（富文本）
     */
    private String content;

    /**
     * 讲师ID
     */
    private Long teacherId;

    /**
     * 讲师名称
     */
    private String teacherName;

    /**
     * 机构ID
     */
    private Long orgId;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 课程类型 1-录播 2-直播 3-图文
     */
    private Integer type;

    /**
     * 课程价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 是否免费 0-收费 1-免费
     */
    private Integer isFree;

    /**
     * 课程状态 0-草稿 1-待审核 2-已发布 3-已下架
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人名称
     */
    private String auditorName;

    /**
     * 课时数
     */
    private Integer lessonCount;

    /**
     * 学习人数
     */
    private Integer studyCount;

    /**
     * 评分
     */
    private BigDecimal score;

    /**
     * 总时长（秒）
     */
    private Long totalDuration;

    /**
     * 排序
     */
    private Integer sort;
}

