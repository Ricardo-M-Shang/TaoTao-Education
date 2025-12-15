package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "课程回答")
public class AnswerVO {

    private Long id;
    private Long questionId;
    private Long courseId;
    private Long userId;
    private String nickname;
    private String username;
    private String content;
    private Integer accepted;
    private LocalDateTime createTime;
}


