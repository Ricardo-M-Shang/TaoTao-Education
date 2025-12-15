package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程评价VO
 */
@Data
@Schema(description = "课程评价")
public class CourseReviewVO {

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "评分")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "是否匿名")
    private Integer isAnonymous;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

