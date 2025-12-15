package com.taotao.education.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.learning.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学习记录Mapper
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {

    /**
     * 获取用户某日学习时长
     */
    @Select("SELECT COALESCE(SUM(study_duration), 0) FROM t_learning_record " +
            "WHERE user_id = #{userId} AND study_date = #{studyDate} AND deleted = 0")
    Integer getDailyStudyDuration(@Param("userId") Long userId, @Param("studyDate") LocalDate studyDate);

    /**
     * 获取用户学习天数统计
     */
    @Select("SELECT COUNT(DISTINCT study_date) FROM t_learning_record " +
            "WHERE user_id = #{userId} AND deleted = 0")
    Integer getUserStudyDays(@Param("userId") Long userId);

    /**
     * 获取用户课程学习统计
     */
    @Select("SELECT " +
            "COUNT(DISTINCT lesson_id) as completedLessons, " +
            "COALESCE(SUM(study_duration), 0) as totalStudyDuration, " +
            "COUNT(DISTINCT study_date) as studyDays " +
            "FROM t_learning_record " +
            "WHERE user_id = #{userId} AND course_id = #{courseId} AND deleted = 0")
    Map<String, Object> getCourseStudyStats(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * 获取用户最近学习记录
     */
    @Select("SELECT * FROM t_learning_record " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY study_date DESC, create_time DESC " +
            "LIMIT #{limit}")
    List<StudyRecord> getRecentStudyRecords(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 获取用户学习时长趋势（最近N天）
     */
    @Select("SELECT study_date, SUM(study_duration) as duration " +
            "FROM t_learning_record " +
            "WHERE user_id = #{userId} " +
            "AND study_date >= #{startDate} " +
            "AND deleted = 0 " +
            "GROUP BY study_date " +
            "ORDER BY study_date")
    List<Map<String, Object>> getStudyTrend(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDate startDate);

    /**
     * 获取用户连续学习天数
     */
    @Select("SELECT study_date FROM t_learning_record " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "GROUP BY study_date " +
            "ORDER BY study_date DESC")
    List<LocalDate> getUserStudyDates(@Param("userId") Long userId);
}
