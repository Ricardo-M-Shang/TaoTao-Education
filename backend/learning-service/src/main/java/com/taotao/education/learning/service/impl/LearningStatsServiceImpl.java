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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
        List<LearningStats> statsList = baseMapper.getUserCourseStats(userId);
        return statsList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningStatsVO> getLearningCourses(Long userId) {
        List<LearningStats> statsList = baseMapper.getLearningCourses(userId);
        return statsList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningStatsVO> getCompletedCourses(Long userId) {
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
        
        Integer completedLessons = (Integer) courseStats.get("completedLessons");
        Integer totalStudyDuration = (Integer) courseStats.get("totalStudyDuration");
        Integer studyDays = (Integer) courseStats.get("studyDays");

        if (stats == null) {
            // 创建新的统计记录
            stats = new LearningStats();
            stats.setUserId(userId);
            stats.setCourseId(courseId);
            stats.setFirstStudyTime(LocalDateTime.now());
        }

        // 更新统计数据
        stats.setCompletedLessons(completedLessons != null ? completedLessons : 0);
        stats.setStudyDuration(totalStudyDuration != null ? totalStudyDuration : 0);
        stats.setStudyDays(studyDays != null ? studyDays : 0);
        stats.setLastStudyTime(LocalDateTime.now());

        // 计算日均学习时长
        if (studyDays != null && studyDays > 0) {
            stats.setAvgDailyDuration(totalStudyDuration / studyDays);
        }

        // TODO: 这里需要调用course-service获取课程总课时数来计算准确的完成率
        // 暂时使用已完成课时数来估算
        if (completedLessons != null && completedLessons > 0) {
            // 假设课程有20个课时（实际应该从课程服务获取）
            stats.setTotalLessons(20);
            stats.setCompletionRate((double) completedLessons / 20 * 100);
            
            // 判断是否完成课程
            if (completedLessons >= 20) {
                stats.setIsFinished(1);
            }
        }

        this.saveOrUpdate(stats);
    }

    @Override
    public void updateUserCourseStats(Long userId) {
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
        return baseMapper.getCompletionRateDistribution(userId);
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
