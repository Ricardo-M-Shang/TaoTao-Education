package com.taotao.education.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.learning.entity.LearningStats;
import com.taotao.education.learning.entity.UserLearningStats;
import com.taotao.education.learning.mapper.StudyRecordMapper;
import com.taotao.education.learning.mapper.UserLearningStatsMapper;
import com.taotao.education.learning.service.LearningStatsService;
import com.taotao.education.learning.service.UserLearningStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 用户学习总统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLearningStatsServiceImpl extends ServiceImpl<UserLearningStatsMapper, UserLearningStats> implements UserLearningStatsService {

    private final LearningStatsService learningStatsService;
    private final StudyRecordMapper studyRecordMapper;

    @Override
    public UserLearningStats getUserStats(Long userId) {
        LambdaQueryWrapper<UserLearningStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLearningStats::getUserId, userId);
        
        UserLearningStats stats = this.getOne(wrapper);
        
        if (stats == null) {
            // 如果没有统计数据，创建初始数据
            updateUserStats(userId);
            stats = this.getOne(wrapper);
        }
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStats(Long userId) {
        // 获取用户现有统计记录
        LambdaQueryWrapper<UserLearningStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLearningStats::getUserId, userId);
        UserLearningStats stats = this.getOne(wrapper);

        if (stats == null) {
            stats = new UserLearningStats();
            stats.setUserId(userId);
        }

        // 统计课程相关数据
        LambdaQueryWrapper<LearningStats> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(LearningStats::getUserId, userId);
        List<LearningStats> courseStatsList = learningStatsService.list(courseWrapper);

        if (!courseStatsList.isEmpty()) {
            // 总课程数
            stats.setTotalCourses(courseStatsList.size());
            
            // 已完成课程数
            long completedCount = courseStatsList.stream()
                    .filter(course -> course.getIsFinished() != null && course.getIsFinished() == 1)
                    .count();
            stats.setCompletedCourses((int) completedCount);

            // 总学习时长
            int totalDuration = courseStatsList.stream()
                    .mapToInt(course -> course.getStudyDuration() != null ? course.getStudyDuration() : 0)
                    .sum();
            stats.setTotalStudyDuration(totalDuration);
        } else {
            stats.setTotalCourses(0);
            stats.setCompletedCourses(0);
            stats.setTotalStudyDuration(0);
        }

        // 获取学习天数
        Integer studyDays = studyRecordMapper.getUserStudyDays(userId);
        stats.setTotalStudyDays(studyDays != null ? studyDays : 0);

        // 计算日均学习时长
        if (stats.getTotalStudyDays() > 0) {
            stats.setAvgDailyDuration(stats.getTotalStudyDuration() / stats.getTotalStudyDays());
        } else {
            stats.setAvgDailyDuration(0);
        }

        // 更新连续学习天数
        updateStreakDays(stats, userId);

        // 计算等级积分（简单算法：学习时长/60 + 完成课程数*100）
        int levelScore = (stats.getTotalStudyDuration() / 60) + (stats.getCompletedCourses() * 100);
        stats.setLevelScore(levelScore);

        // 设置最后学习日期
        if (studyDays > 0) {
            stats.setLastStudyDate(LocalDate.now());
        }

        this.saveOrUpdate(stats);
    }

    @Override
    public void updateUserStreak(Long userId) {
        UserLearningStats stats = getUserStats(userId);
        if (stats != null) {
            updateStreakDays(stats, userId);
            this.updateById(stats);
        }
    }

    /**
     * 更新连续学习天数
     */
    private void updateStreakDays(UserLearningStats stats, Long userId) {
        // 获取用户的学习日期列表
        List<LocalDate> studyDates = studyRecordMapper.getUserStudyDates(userId);
        
        if (studyDates == null || studyDates.isEmpty()) {
            stats.setCurrentStreak(0);
            stats.setLongestStreak(0);
            return;
        }

        // 计算当前连续天数
        int currentStreak = 0;
        int maxStreak = 0;
        int tempStreak = 1;

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 检查是否包含今天或昨天
        if (studyDates.contains(today)) {
            currentStreak = 1;
        } else if (studyDates.contains(yesterday)) {
            currentStreak = 1;
        } else {
            currentStreak = 0;
        }

        // 从最近的日期开始，向前计算连续天数
        if (currentStreak > 0) {
            LocalDate checkDate = studyDates.contains(today) ? today : yesterday;
            
            for (int i = 1; i < studyDates.size(); i++) {
                LocalDate prevDate = checkDate.minusDays(i);
                if (studyDates.contains(prevDate)) {
                    currentStreak++;
                } else {
                    break;
                }
            }
        }

        // 计算最长连续天数
        for (int i = 1; i < studyDates.size(); i++) {
            LocalDate currentDate = studyDates.get(i - 1);
            LocalDate nextDate = studyDates.get(i);
            
            if (ChronoUnit.DAYS.between(nextDate, currentDate) == 1) {
                tempStreak++;
            } else {
                maxStreak = Math.max(maxStreak, tempStreak);
                tempStreak = 1;
            }
        }
        maxStreak = Math.max(maxStreak, tempStreak);

        stats.setCurrentStreak(currentStreak);
        stats.setLongestStreak(Math.max(maxStreak, currentStreak));
    }
}
