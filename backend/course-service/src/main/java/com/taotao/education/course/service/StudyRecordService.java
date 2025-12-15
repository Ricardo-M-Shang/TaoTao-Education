package com.taotao.education.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.course.dto.StudyProgressDTO;
import com.taotao.education.course.entity.StudyRecord;
import com.taotao.education.course.vo.RecentStudyRecordVO;
import com.taotao.education.course.vo.StudyRecordVO;
import com.taotao.education.course.vo.StudyStatisticsVO;

import java.util.List;

/**
 * 学习记录服务接口
 */
public interface StudyRecordService extends IService<StudyRecord> {

    /**
     * 更新学习进度
     */
    void updateProgress(Long userId, StudyProgressDTO dto);

    /**
     * 获取课程学习记录
     */
    List<StudyRecordVO> getCourseStudyRecords(Long userId, Long courseId);

    /**
     * 获取课时学习记录
     */
    StudyRecordVO getLessonStudyRecord(Long userId, Long lessonId);

    /**
     * 计算课程学习进度（百分比）
     */
    int calculateCourseProgress(Long userId, Long courseId);

    /**
     * 获取学习统计数据
     */
    StudyStatisticsVO getStudyStatistics(Long userId);

    /**
     * 获取最近学习记录
     */
    List<RecentStudyRecordVO> getRecentStudyRecords(Long userId, int limit);
}

