package com.taotao.education.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建课程问题")
public class QuestionCreateDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "问题内容不能为空")
    private String content;
}


