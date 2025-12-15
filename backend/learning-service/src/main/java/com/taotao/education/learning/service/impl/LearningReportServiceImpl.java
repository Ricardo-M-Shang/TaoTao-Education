package com.taotao.education.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taotao.education.learning.dto.LearningReportQueryDTO;
import com.taotao.education.learning.mapper.StudyRecordMapper;
import com.taotao.education.learning.service.LearningReportService;
import com.taotao.education.learning.service.LearningStatsService;
import com.taotao.education.learning.vo.LearningReportVO;
import com.taotao.education.learning.vo.LearningStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习报告服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningReportServiceImpl implements LearningReportService {

    private final StudyRecordMapper studyRecordMapper;
    private final LearningStatsService learningStatsService;

    @Override
    public LearningReportVO generateLearningReport(Long userId, LearningReportQueryDTO queryDTO) {
        LearningReportVO report = new LearningReportVO();
        
        // 确定时间范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, queryDTO.getPeriod());
        
        report.setPeriod(queryDTO.getPeriod());
        report.setStartDate(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.setEndDate(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // 获取基础统计数据
        fillBasicStats(report, userId, startDate, endDate);
        
        // 获取趋势数据
        report.setDurationTrend(getStudyDurationTrend(userId, queryDTO.getPeriod()));
        
        // 获取分布数据
        report.setCompletionDistribution(learningStatsService.getCompletionRateDistribution(userId));
        report.setTimeDistribution(getStudyTimeDistribution(userId, queryDTO.getPeriod()));
        
        // 获取课程数据
        report.setRecentCourses(getRecentCourses(userId, 5));
        report.setActiveCourses(getActiveCourses(userId, startDate, endDate, 3));
        report.setNewCompletedCourses(getNewCompletedCourses(userId, startDate, endDate));
        
        // 获取等级和建议
        report.setLevel(calculateUserLevel(userId));
        report.setSuggestions(getLearningAdvice(userId));

        return report;
    }

    @Override
    public List<Map<String, Object>> getStudyDurationTrend(Long userId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, period);
        
        return studyRecordMapper.getStudyTrend(userId, startDate);
    }

    @Override
    public List<Map<String, Object>> getStudyTimeDistribution(Long userId, String period) {
        // 这里可以实现学习时段分布统计
        // 比如统计用户在一天中各个时段的学习分布
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        // 示例数据，实际需要从数据库统计
        String[] timeSlots = {"06:00-09:00", "09:00-12:00", "12:00-14:00", 
                             "14:00-17:00", "17:00-20:00", "20:00-23:00"};
        String[] slotNames = {"早晨", "上午", "午后", "下午", "傍晚", "夜晚"};
        
        for (int i = 0; i < timeSlots.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("timeSlot", timeSlots[i]);
            item.put("name", slotNames[i]);
            item.put("duration", (int)(Math.random() * 3600)); // 示例数据
            distribution.add(item);
        }
        
        return distribution;
    }

    @Override
    public List<String> getLearningAdvice(Long userId) {
        List<String> advice = new ArrayList<>();
        
        // 获取用户课程学习统计
        List<LearningStatsVO> userCourseStats = learningStatsService.getUserCourseStats(userId);
        
        if (userCourseStats.isEmpty()) {
            advice.add("开始您的学习之旅吧！选择一门感兴趣的课程");
            return advice;
        }

        // 计算基础统计
        long completedCount = userCourseStats.stream()
            .filter(stats -> stats.getIsFinished() != null && stats.getIsFinished())
            .count();
        
        int totalStudyDuration = userCourseStats.stream()
            .mapToInt(stats -> stats.getStudyDuration() != null ? stats.getStudyDuration() : 0)
            .sum();
            
        int maxStudyDays = userCourseStats.stream()
            .mapToInt(stats -> stats.getStudyDays() != null ? stats.getStudyDays() : 0)
            .max().orElse(0);

        // 根据学习数据生成建议
        if (maxStudyDays == 0) {
            advice.add("建议保持每日学习习惯，连续学习能提高学习效果");
        } else if (maxStudyDays < 7) {
            advice.add("很好！继续保持学习节奏，争取连续学习一周");
        } else {
            advice.add("太棒了！您已经坚持学习了" + maxStudyDays + "天，请继续保持");
        }

        int avgDailyDuration = maxStudyDays > 0 ? totalStudyDuration / maxStudyDays : 0;
        if (avgDailyDuration < 1800) { // 30分钟
            advice.add("建议每日学习时长达到30分钟以上，会有更好的学习效果");
        }

        if (completedCount < userCourseStats.size() / 2) {
            advice.add("您有" + (userCourseStats.size() - completedCount) + "门课程尚未完成，建议专注完成现有课程");
        }

        return advice;
    }

    @Override
    public Integer calculateUserLevel(Long userId) {
        // 获取用户课程学习统计
        List<LearningStatsVO> userCourseStats = learningStatsService.getUserCourseStats(userId);
        
        if (userCourseStats.isEmpty()) {
            return 1;
        }
        
        // 计算总学习时长和完成课程数
        int totalStudyDuration = userCourseStats.stream()
            .mapToInt(stats -> stats.getStudyDuration() != null ? stats.getStudyDuration() : 0)
            .sum();
            
        long completedCount = userCourseStats.stream()
            .filter(stats -> stats.getIsFinished() != null && stats.getIsFinished())
            .count();
        
        // 根据学习时长和完成课程数计算等级积分
        int levelScore = (totalStudyDuration / 60) + ((int) completedCount * 100);
        
        // 等级计算规则：每1000积分升一级
        return Math.max(1, levelScore / 1000 + 1);
    }

    /**
     * 计算开始日期
     */
    private LocalDate calculateStartDate(LocalDate endDate, String period) {
        return switch (period) {
            case "week" -> endDate.minusWeeks(1);
            case "month" -> endDate.minusMonths(1);
            case "year" -> endDate.minusYears(1);
            default -> endDate.minusWeeks(1);
        };
    }

    /**
     * 填充基础统计数据
     */
    private void fillBasicStats(LearningReportVO report, Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取用户课程统计数据
        List<LearningStatsVO> userCourseStats = learningStatsService.getUserCourseStats(userId);
        
        if (!userCourseStats.isEmpty()) {
            // 计算总学习时长
            int totalDuration = userCourseStats.stream()
                .mapToInt(stats -> stats.getStudyDuration() != null ? stats.getStudyDuration() : 0)
                .sum();
            
            // 计算已完成课程数
            long completedCount = userCourseStats.stream()
                .filter(stats -> stats.getIsFinished() != null && stats.getIsFinished())
                .count();
            
            // 计算总学习天数
            int totalStudyDays = userCourseStats.stream()
                .mapToInt(stats -> stats.getStudyDays() != null ? stats.getStudyDays() : 0)
                .sum();
            
            report.setTotalStudyDuration(totalDuration);
            report.setTotalStudyDays(totalStudyDays);
            report.setStudyCourseCount(userCourseStats.size());
            report.setCompletedCourseCount((int) completedCount);
            report.setAvgDailyDuration(totalStudyDays > 0 ? totalDuration / totalStudyDays : 0);
            report.setCurrentStreak(0); // 暂时设为0，后续可以实现连续学习天数计算
        } else {
            // 设置默认值
            report.setTotalStudyDuration(0);
            report.setTotalStudyDays(0);
            report.setStudyCourseCount(0);
            report.setCompletedCourseCount(0);
            report.setAvgDailyDuration(0);
            report.setCurrentStreak(0);
        }
    }

    /**
     * 获取最近学习的课程
     */
    private List<LearningStatsVO> getRecentCourses(Long userId, Integer limit) {
        List<LearningStatsVO> allCourses = learningStatsService.getUserCourseStats(userId);
        return allCourses.stream()
                .filter(course -> course.getLastStudyTime() != null)
                .sorted((a, b) -> b.getLastStudyTime().compareTo(a.getLastStudyTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取最活跃的课程（本周期学习时长最多）
     */
    private List<LearningStatsVO> getActiveCourses(Long userId, LocalDate startDate, LocalDate endDate, Integer limit) {
        List<LearningStatsVO> allCourses = learningStatsService.getUserCourseStats(userId);
        return allCourses.stream()
                .sorted((a, b) -> Integer.compare(
                    b.getStudyDuration() != null ? b.getStudyDuration() : 0,
                    a.getStudyDuration() != null ? a.getStudyDuration() : 0))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取新完成的课程
     */
    private List<LearningStatsVO> getNewCompletedCourses(Long userId, LocalDate startDate, LocalDate endDate) {
        List<LearningStatsVO> completedCourses = learningStatsService.getCompletedCourses(userId);
        // 这里应该筛选在指定时间范围内完成的课程
        // 暂时返回所有已完成的课程
        return completedCourses;
    }
}
