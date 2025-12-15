package com.taotao.education.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程审核 DTO
 */
@Data
@Schema(description = "课程审核请求")
public class CourseAuditDTO {

    @Schema(description = "审核备注")
    private String remark;
}


