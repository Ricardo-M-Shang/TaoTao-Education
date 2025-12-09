package com.taotao.education.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存笔记DTO
 */
@Data
public class NoteSaveDTO {

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 课时ID
     */
    @NotNull(message = "课时ID不能为空")
    private Long lessonId;

    /**
     * 笔记内容
     */
    @NotBlank(message = "笔记内容不能为空")
    private String content;

    /**
     * 视频时间点（秒）
     */
    private Integer videoTime;
}

