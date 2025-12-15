package com.taotao.education.user.vo;

import lombok.Data;

/**
 * 运营端用户概览
 */
@Data
public class OpsUserOverviewVO {

    /**
     * 平台用户总数
     */
    private Long total;

    /**
     * 学员数
     */
    private Long students;

    /**
     * 讲师数
     */
    private Long teachers;

    /**
     * 机构数
     */
    private Long orgs;

    /**
     * 运营账号数
     */
    private Long ops;
}


