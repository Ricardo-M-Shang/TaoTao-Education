package com.taotao.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.course.dto.StudyProgressDTO;
import com.taotao.education.course.entity.Lesson;
import com.taotao.education.course.entity.StudyRecord;
import com.taotao.education.course.mapper.LessonMapper;
import com.taotao.education.course.mapper.StudyRecordMapper;
import com.taotao.education.course.service.StudyRecordService;
import com.taotao.education.course.vo.RecentStudyRecordVO;
import com.taotao.education.course.vo.StudyRecordVO;
import com.taotao.education.course.vo.StudyStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class StudyRecordServiceImpl extends ServiceImpl<StudyRecordMapper, StudyRecord> implements StudyRecordService {

    private final LessonMapper lessonMapper;

    @Override
    public void updateProgress(Long userId, StudyProgressDTO dto) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .eq(StudyRecord::getLessonId, dto.getLessonId());
        StudyRecord record = this.getOne(wrapper);

        if (record == null) {
            // 创建新记录
            record = new StudyRecord();
            record.setUserId(userId);
            record.setCourseId(dto.getCourseId());
            record.setLessonId(dto.getLessonId());
            record.setChapterId(dto.getChapterId());
            record.setDuration(dto.getDuration() != null ? dto.getDuration() : 0);
            record.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
            record.setIsFinished(dto.getProgress() != null && dto.getProgress() >= 90 ? 1 : 0);
            this.save(record);
        } else {
            // 更新记录
            if (dto.getDuration() != null) {
                record.setDuration(record.getDuration() + dto.getDuration());
            }
            if (dto.getProgress() != null && dto.getProgress() > record.getProgress()) {
                record.setProgress(dto.getProgress());
            }
            if (record.getProgress() >= 90) {
                record.setIsFinished(1);
            }
            this.updateById(record);
        }
    }

    @Override
    public List<StudyRecordVO> getCourseStudyRecords(Long userId, Long courseId) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .eq(StudyRecord::getCourseId, courseId);

        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public StudyRecordVO getLessonStudyRecord(Long userId, Long lessonId) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .eq(StudyRecord::getLessonId, lessonId);
        StudyRecord record = this.getOne(wrapper);

        if (record == null) {
            return null;
        }
        return convertToVO(record);
    }

    @Override
    public int calculateCourseProgress(Long userId, Long courseId) {
        // 获取课程所有课时
        LambdaQueryWrapper<Lesson> lessonWrapper = new LambdaQueryWrapper<>();
        lessonWrapper.eq(Lesson::getCourseId, courseId);
        long totalLessons = lessonMapper.selectCount(lessonWrapper);

        if (totalLessons == 0) {
            return 0;
        }

        // 获取已完成的课时数
        LambdaQueryWrapper<StudyRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(StudyRecord::getUserId, userId)
                     .eq(StudyRecord::getCourseId, courseId)
                     .eq(StudyRecord::getIsFinished, 1);
        long finishedLessons = this.count(recordWrapper);

        return (int) (finishedLessons * 100 / totalLessons);
    }

    private StudyRecordVO convertToVO(StudyRecord record) {
        StudyRecordVO vo = new StudyRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    @Override
    public StudyStatisticsVO getStudyStatistics(Long userId) {
        StudyStatisticsVO vo = new StudyStatisticsVO();
        
        // 获取总学习时长
        Integer totalStudyTime = baseMapper.getTotalStudyTime(userId);
        vo.setTotalStudyTime(totalStudyTime != null ? totalStudyTime : 0);
        
        // 获取学习课程数
        Integer totalCourses = baseMapper.getTotalCourses(userId);
        vo.setTotalCourses(totalCourses != null ? totalCourses : 0);
        
        // 获取已完成课程数（这里简化处理，以课程进度>=90%为完成标准）
        // 实际可以通过user_course表的is_finished字段判断
        LambdaQueryWrapper<StudyRecord> finishedWrapper = new LambdaQueryWrapper<>();
        finishedWrapper.eq(StudyRecord::getUserId, userId)
                       .eq(StudyRecord::getIsFinished, 1)
                       .select(StudyRecord::getCourseId)
                       .groupBy(StudyRecord::getCourseId);
        // 统计有多少门课程的所有课时都完成了
        int finishedCourses = calculateFinishedCourses(userId);
        vo.setFinishedCourses(finishedCourses);
        
        // 获取学习天数
        Integer studyDays = baseMapper.getStudyDays(userId);
        vo.setStudyDays(studyDays != null ? studyDays : 0);
        
        // 获取最近7天学习时长
        List<Integer> weeklyStudyTime = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Integer dayTime = baseMapper.getDayStudyTime(userId, i);
            weeklyStudyTime.add(dayTime != null ? dayTime : 0);
        }
        vo.setWeeklyStudyTime(weeklyStudyTime);
        
        return vo;
    }

    /**
     * 计算已完成的课程数
     * 判断标准：课程的所有课时都已完成
     */
    private int calculateFinishedCourses(Long userId) {
        // 获取用户学习过的所有课程ID
        LambdaQueryWrapper<StudyRecord> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(StudyRecord::getUserId, userId)
                     .select(StudyRecord::getCourseId)
                     .groupBy(StudyRecord::getCourseId);
        List<StudyRecord> courseRecords = this.list(courseWrapper);
        
        int finishedCount = 0;
        for (StudyRecord record : courseRecords) {
            // 计算该课程的学习进度
            int progress = calculateCourseProgress(userId, record.getCourseId());
            if (progress >= 100) {
                finishedCount++;
            }
        }
        return finishedCount;
    }

    @Override
    public List<RecentStudyRecordVO> getRecentStudyRecords(Long userId, int limit) {
        return baseMapper.getRecentStudyRecords(userId, limit);
    }
}

