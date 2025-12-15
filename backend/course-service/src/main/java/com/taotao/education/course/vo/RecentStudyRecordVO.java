package com.taotao.education.course.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 最近学习记录VO
 */
@Data
public class RecentStudyRecordVO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程标题
     */
    private String courseTitle;
    
    /**
     * 课程封面
     */
    private String courseCover;
    
    /**
     * 课时ID
     */
    private Long lessonId;
    
    /**
     * 课时标题
     */
    private String lessonTitle;
    
    /**
     * 学习进度（百分比）
     */
    private Integer progress;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

