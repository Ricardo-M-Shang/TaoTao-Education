package com.taotao.education.learning.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.learning.dto.StudyProgressDTO;
import com.taotao.education.learning.entity.StudyRecord;
import com.taotao.education.learning.vo.StudyRecordVO;

import java.util.List;

/**
 * 学习记录服务接口
 */
public interface StudyRecordService extends IService<StudyRecord> {

    /**
     * 记录学习进度
     */
    void recordStudyProgress(Long userId, StudyProgressDTO dto);

    /**
     * 获取用户学习记录分页列表
     */
    Page<StudyRecordVO> getUserStudyRecords(Long userId, Long courseId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户最近学习记录
     */
    List<StudyRecordVO> getRecentStudyRecords(Long userId, Integer limit);

    /**
     * 获取课程学习进度
     */
    Double getCourseProgress(Long userId, Long courseId);

    /**
     * 获取课时学习进度
     */
    StudyRecord getLessonProgress(Long userId, Long lessonId);

    /**
     * 批量更新学习统计（定时任务调用）
     */
    void updateLearningStats();
}
