package com.taotao.education.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.learning.entity.LearningStats;
import com.taotao.education.learning.entity.StudyRecord;
import com.taotao.education.learning.mapper.LearningStatsMapper;
import com.taotao.education.learning.mapper.StudyRecordMapper;
import com.taotao.education.learning.service.LearningStatsService;
import com.taotao.education.learning.vo.LearningStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学习统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningStatsServiceImpl extends ServiceImpl<LearningStatsMapper, LearningStats> implements LearningStatsService {

    private final StudyRecordMapper studyRecordMapper;

    @Override
    public LearningStatsVO getCourseStats(Long userId, Long courseId) {
        syncSingleCourseStatsFromRecords(userId, courseId);
        LambdaQueryWrapper<LearningStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningStats::getUserId, userId)
                .eq(LearningStats::getCourseId, courseId);
        
        LearningStats stats = this.getOne(wrapper);
        if (stats == null) {
            // 如果没有统计数据，创建初始数据
            updateCourseStats(userId, courseId);
            stats = this.getOne(wrapper);
        }
        
        return convertToVO(stats);
    }

    @Override
    public List<LearningStatsVO> getUserCourseStats(Long userId) {
        syncUserCourseStatsFromRecords(userId);
        List<LearningStats> statsList = baseMapper.getUserCourseStats(userId);
        return statsList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningStatsVO> getLearningCourses(Long userId) {
        syncUserCourseStatsFromRecords(userId);
        List<LearningStats> statsList = baseMapper.getLearningCourses(userId);
        return statsList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningStatsVO> getCompletedCourses(Long userId) {
        syncUserCourseStatsFromRecords(userId);
        List<LearningStats> statsList = baseMapper.getCompletedCourses(userId);
        return statsList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStats(Long userId, Long courseId) {
        // 获取课程学习统计数据
        Map<String, Object> courseStats = studyRecordMapper.getCourseStudyStats(userId, courseId);
        
        if (courseStats == null || courseStats.isEmpty()) {
            return;
        }

        // 查找现有统计记录
        LambdaQueryWrapper<LearningStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningStats::getUserId, userId)
                .eq(LearningStats::getCourseId, courseId);
        
        LearningStats stats = this.getOne(wrapper);
        
        int completedLessons = toInt(courseStats.get("completedLessons"));
        int totalStudyDuration = toInt(courseStats.get("totalStudyDuration"));
        int studyDays = toInt(courseStats.get("studyDays"));

        if (stats == null) {
            // 创建新的统计记录
            stats = new LearningStats();
            stats.setUserId(userId);
            stats.setCourseId(courseId);
            stats.setFirstStudyTime(LocalDateTime.now());
        }

        // 更新统计数据
        stats.setCompletedLessons(completedLessons);
        stats.setStudyDuration(totalStudyDuration);
        stats.setStudyDays(studyDays);
        stats.setLastStudyTime(LocalDateTime.now());
        stats.setIsFinished(0);

        // 计算日均学习时长
        if (studyDays > 0) {
            stats.setAvgDailyDuration(totalStudyDuration / studyDays);
        } else {
            stats.setAvgDailyDuration(0);
        }

        // TODO: 这里需要调用course-service获取课程总课时数来计算准确的完成率
        // 暂时使用已完成课时数来估算
        stats.setTotalLessons(20);
        stats.setCompletionRate((double) completedLessons / 20 * 100);
        if (completedLessons >= 20) {
            stats.setIsFinished(1);
        }

        this.saveOrUpdate(stats);
    }

    @Override
    public void updateUserCourseStats(Long userId) {
        syncUserCourseStatsFromRecords(userId);
        // 获取用户所有课程的学习统计并更新
        List<LearningStats> allStats = baseMapper.selectList(
            new LambdaQueryWrapper<LearningStats>()
                .eq(LearningStats::getUserId, userId)
                .eq(LearningStats::getDeleted, 0)
        );
        
        for (LearningStats stats : allStats) {
            updateCourseStats(userId, stats.getCourseId());
        }
    }

    @Override
    public List<Map<String, Object>> getCompletionRateDistribution(Long userId) {
        syncUserCourseStatsFromRecords(userId);
        return baseMapper.getCompletionRateDistribution(userId);
    }

    private void syncUserCourseStatsFromRecords(Long userId) {
        Set<Long> courseIds = new HashSet<>();
        List<Long> learningCourseIds = studyRecordMapper.getUserStudiedCourseIds(userId);
        if (learningCourseIds != null) {
            courseIds.addAll(learningCourseIds);
        }
        List<Long> courseServiceCourseIds = studyRecordMapper.getUserStudiedCourseIdsFromCourseService(userId);
        if (courseServiceCourseIds != null) {
            courseIds.addAll(courseServiceCourseIds);
        }
        if (courseIds.isEmpty()) {
            return;
        }
        for (Long courseId : courseIds.stream().filter(Objects::nonNull).toList()) {
            try {
                syncSingleCourseStatsFromRecords(userId, courseId);
            } catch (Exception e) {
                log.error("同步课程学习统计失败: userId={}, courseId={}", userId, courseId, e);
            }
        }
    }

    private void syncSingleCourseStatsFromRecords(Long userId, Long courseId) {
        importCourseServiceRecordsIfNeeded(userId, courseId);
        updateCourseStats(userId, courseId);
    }

    private void importCourseServiceRecordsIfNeeded(Long userId, Long courseId) {
        Integer count = studyRecordMapper.countLearningRecordsByCourse(userId, courseId);
        if (count != null && count > 0) {
            return;
        }

        List<Map<String, Object>> sourceRecords = studyRecordMapper.getCourseServiceStudyRecords(userId, courseId);
        if (sourceRecords == null || sourceRecords.isEmpty()) {
            return;
        }

        for (Map<String, Object> source : sourceRecords) {
            StudyRecord record = new StudyRecord();
            record.setUserId(userId);
            record.setCourseId(courseId);
            record.setLessonId(toLong(source.get("lessonId")));

            int duration = toInt(source.get("duration"));
            int progress = toInt(source.get("progress"));
            int isFinished = toInt(source.get("isFinished"));
            LocalDateTime updateTime = toLocalDateTime(source.get("updateTime"));

            record.setStudyDuration(duration);
            record.setVideoDuration(Math.max(duration, 0));
            record.setLastPosition(Math.max(duration, 0));
            record.setProgressPercent((double) Math.max(progress, 0));
            record.setIsCompleted(isFinished > 0 ? 1 : 0);
            record.setStudyDate(updateTime != null ? updateTime.toLocalDate() : LocalDate.now());
            record.setDeviceType("course-sync");

            if (updateTime != null) {
                record.setCreateTime(updateTime);
                record.setUpdateTime(updateTime);
            }
            studyRecordMapper.insert(record);
        }
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
        }

        String text = value.toString();
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(text);
            } catch (DateTimeParseException ignore) {
                log.warn("无法解析时间格式: {}", text);
                return null;
            }
        }
    }

    /**
     * 转换为VO对象
     */
    private LearningStatsVO convertToVO(LearningStats stats) {
        if (stats == null) {
            return null;
        }

        LearningStatsVO vo = new LearningStatsVO();
        BeanUtils.copyProperties(stats, vo);
        
        vo.setIsFinished(stats.getIsFinished() != null && stats.getIsFinished() == 1);
        
        // 设置学习状态
        if (stats.getCompletionRate() == null || stats.getCompletionRate() == 0) {
            vo.setStatus("not_started");
        } else if (stats.getCompletionRate() < 100) {
            vo.setStatus("learning");
        } else {
            vo.setStatus("completed");
        }

        // TODO: 这里需要调用course-service获取课程详细信息
        // 暂时使用占位符
        vo.setCourseTitle("课程标题");
        vo.setCourseCover("课程封面URL");
        vo.setTeacherName("讲师姓名");

        return vo;
    }
}
