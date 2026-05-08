package com.taotao.education.learning.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.learning.dto.StudyProgressDTO;
import com.taotao.education.learning.entity.StudyRecord;
import com.taotao.education.learning.mapper.StudyRecordMapper;
import com.taotao.education.learning.service.LearningStatsService;
import com.taotao.education.learning.service.StudyRecordService;
import com.taotao.education.learning.vo.StudyRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学习记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyRecordServiceImpl extends ServiceImpl<StudyRecordMapper, StudyRecord> implements StudyRecordService {

    private final LearningStatsService learningStatsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordStudyProgress(Long userId, StudyProgressDTO dto) {
        // 查找今日该课时的学习记录
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
                .eq(StudyRecord::getCourseId, dto.getCourseId())
                .eq(StudyRecord::getLessonId, dto.getLessonId())
                .eq(StudyRecord::getStudyDate, LocalDate.now());

        StudyRecord existRecord = this.getOne(wrapper);

        if (existRecord != null) {
            // 更新现有记录
            existRecord.setStudyDuration(existRecord.getStudyDuration() + (dto.getStudyDuration() != null ? dto.getStudyDuration() : 0));
            existRecord.setLastPosition(dto.getLastPosition());
            if (dto.getVideoDuration() != null) {
                existRecord.setVideoDuration(dto.getVideoDuration());
                existRecord.setProgressPercent((double) dto.getLastPosition() / dto.getVideoDuration() * 100);
            }
            existRecord.setIsCompleted(dto.getIsCompleted());
            if (StrUtil.isNotBlank(dto.getDeviceType())) {
                existRecord.setDeviceType(dto.getDeviceType());
            }
            this.updateById(existRecord);
        } else {
            // 创建新记录
            StudyRecord record = new StudyRecord();
            BeanUtils.copyProperties(dto, record);
            record.setUserId(userId);
            record.setStudyDate(LocalDate.now());
            if (dto.getVideoDuration() != null && dto.getVideoDuration() > 0) {
                record.setProgressPercent((double) dto.getLastPosition() / dto.getVideoDuration() * 100);
            }
            this.save(record);
        }

        // 异步更新课程学习统计
        updateCourseStatsAsync(userId, dto.getCourseId());
    }

    @Override
    public Page<StudyRecordVO> getUserStudyRecords(Long userId, Long courseId, Integer pageNum, Integer pageSize) {
        Page<StudyRecord> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);
        if (courseId != null) {
            wrapper.eq(StudyRecord::getCourseId, courseId);
        }
        wrapper.orderByDesc(StudyRecord::getStudyDate, StudyRecord::getCreateTime);

        Page<StudyRecord> recordPage = this.page(page, wrapper);

        // 转换为VO
        Page<StudyRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        List<StudyRecordVO> voList = recordPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public List<StudyRecordVO> getRecentStudyRecords(Long userId, Integer limit) {
        List<StudyRecord> records = baseMapper.getRecentStudyRecords(userId, limit != null ? limit : 10);
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Double getCourseProgress(Long userId, Long courseId) {
        // 查询课程学习统计
        Map<String, Object> stats = baseMapper.getCourseStudyStats(userId, courseId);
        
        if (stats == null || stats.isEmpty()) {
            return 0.0;
        }

        // 这里需要调用course-service获取课程总课时数来计算准确的进度
        // 暂时使用已完成课时数作为进度参考
        Object completedLessons = stats.get("completedLessons");
        if (completedLessons instanceof Number number) {
            return number.doubleValue();
        }
        return completedLessons != null ? Double.parseDouble(completedLessons.toString()) : 0.0;
    }

    @Override
    public StudyRecord getLessonProgress(Long userId, Long lessonId) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
                .eq(StudyRecord::getLessonId, lessonId)
                .orderByDesc(StudyRecord::getStudyDate)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Async
    public void updateLearningStats() {
        log.info("开始更新学习统计数据...");
        // 这里可以实现批量更新逻辑，比如每日定时更新所有用户的学习统计
        log.info("学习统计数据更新完成");
    }

    /**
     * 异步更新课程统计
     */
    @Async
    public void updateCourseStatsAsync(Long userId, Long courseId) {
        try {
            learningStatsService.updateCourseStats(userId, courseId);
        } catch (Exception e) {
            log.error("更新课程统计失败: userId={}, courseId={}", userId, courseId, e);
        }
    }

    /**
     * 转换为VO对象
     */
    private StudyRecordVO convertToVO(StudyRecord record) {
        StudyRecordVO vo = new StudyRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setIsCompleted(record.getIsCompleted() != null && record.getIsCompleted() == 1);
        
        // TODO: 这里需要调用course-service获取课程和课时的详细信息
        // 暂时使用占位符
        vo.setCourseTitle("课程标题"); // 实际需要通过courseId查询
        vo.setLessonTitle("课时标题"); // 实际需要通过lessonId查询
        
        return vo;
    }
}
