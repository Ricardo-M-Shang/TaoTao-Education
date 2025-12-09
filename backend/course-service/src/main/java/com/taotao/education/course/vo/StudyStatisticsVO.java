package com.taotao.education.course.vo;

import lombok.Data;
import java.util.List;

/**
 * 学习统计VO
 */
@Data
public class StudyStatisticsVO {
    
    /**
     * 累计学习时长（分钟）
     */
    private Integer totalStudyTime;
    
    /**
     * 学习课程数
     */
    private Integer totalCourses;
    
    /**
     * 已完成课程数
     */
    private Integer finishedCourses;
    
    /**
     * 学习天数
     */
    private Integer studyDays;
    
    /**
     * 最近7天学习时长（分钟）
     */
    private List<Integer> weeklyStudyTime;
}

