package com.taotao.education.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.course.entity.StudyRecord;
import com.taotao.education.course.vo.RecentStudyRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学习记录Mapper
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {

    /**
     * 获取用户总学习时长（分钟）
     */
    @Select("SELECT COALESCE(SUM(duration), 0) / 60 FROM t_study_record WHERE user_id = #{userId}")
    Integer getTotalStudyTime(@Param("userId") Long userId);

    /**
     * 获取用户学习课程数
     */
    @Select("SELECT COUNT(DISTINCT course_id) FROM t_study_record WHERE user_id = #{userId}")
    Integer getTotalCourses(@Param("userId") Long userId);

    /**
     * 获取用户学习天数
     */
    @Select("SELECT COUNT(DISTINCT DATE(update_time)) FROM t_study_record WHERE user_id = #{userId}")
    Integer getStudyDays(@Param("userId") Long userId);

    /**
     * 获取最近N天每天的学习时长（分钟）
     */
    @Select("""
        SELECT COALESCE(SUM(duration), 0) / 60 as minutes
        FROM t_study_record
        WHERE user_id = #{userId}
          AND DATE(update_time) = DATE(DATE_SUB(NOW(), INTERVAL #{dayOffset} DAY))
        """)
    Integer getDayStudyTime(@Param("userId") Long userId, @Param("dayOffset") int dayOffset);

    /**
     * 获取最近学习记录
     */
    @Select("""
        SELECT sr.id, sr.course_id, c.title as course_title, c.cover as course_cover,
               sr.lesson_id, l.title as lesson_title, sr.progress, sr.update_time
        FROM t_study_record sr
        LEFT JOIN t_course c ON sr.course_id = c.id
        LEFT JOIN t_lesson l ON sr.lesson_id = l.id
        WHERE sr.user_id = #{userId}
        ORDER BY sr.update_time DESC
        LIMIT #{limit}
        """)
    List<RecentStudyRecordVO> getRecentStudyRecords(@Param("userId") Long userId, @Param("limit") int limit);
}

