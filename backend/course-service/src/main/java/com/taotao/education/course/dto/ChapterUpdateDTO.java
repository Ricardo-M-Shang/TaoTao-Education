package com.taotao.education.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChapterUpdateDTO {
    @NotBlank(message = "章节标题不能为空")
    private String title;
    private Integer sort;
}

