package com.taotao.education.course.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 笔记VO
 */
@Data
public class NoteVO {

    /**
     * 笔记ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课时ID
     */
    private Long lessonId;

    /**
     * 课时标题
     */
    private String lessonTitle;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 视频时间点（秒）
     */
    private Integer videoTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

