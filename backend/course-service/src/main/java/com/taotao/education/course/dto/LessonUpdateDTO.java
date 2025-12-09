package com.taotao.education.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonUpdateDTO {
    @NotBlank(message = "课时标题不能为空")
    private String title;

    private String videoUrl;
    private Long duration;
    private Integer isFree;
    private Integer sort;
    private Integer type;
}

