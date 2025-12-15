package com.taotao.education.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "课程问题")
public class QuestionVO {

    private Long id;
    private Long courseId;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private LocalDateTime createTime;

    private Integer answerCount;
    private List<AnswerVO> answers;
}