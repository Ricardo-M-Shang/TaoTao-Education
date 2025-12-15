package com.taotao.education.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonCreateDTO {
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @NotBlank(message = "课时标题不能为空")
    private String title;

    private String videoUrl;
    private Long duration;
    private Integer isFree;
    private Integer sort;
    private Integer type = 1; // 1-视频 2-图文
}

