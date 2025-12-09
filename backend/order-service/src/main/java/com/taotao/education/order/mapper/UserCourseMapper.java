package com.taotao.education.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.order.entity.UserCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户课程Mapper接口
 */
@Mapper
public interface UserCourseMapper extends BaseMapper<UserCourse> {

    /**
     * 查询讲师某课程的学员列表
     */
    @org.apache.ibatis.annotations.Select("""
        SELECT uc.user_id         AS userId,
               COALESCE(o.username, u.username) AS username,
               COALESCE(u.nickname, u.username) AS nickname,
               uc.course_id       AS courseId,
               o.course_title     AS courseTitle,
               uc.progress        AS progress,
               uc.is_finished     AS isFinished,
               uc.last_study_time AS lastStudyTime,
               o.pay_time         AS payTime
        FROM t_user_course uc
        LEFT JOIN t_order o ON uc.order_id = o.id
        LEFT JOIN taotao_education.t_user u ON u.id = uc.user_id
        WHERE uc.course_id = #{courseId} AND o.teacher_id = #{teacherId} AND o.status = 1
        ORDER BY o.pay_time DESC
        """)
    java.util.List<com.taotao.education.order.vo.TeacherStudentVO> listStudentsByCourse(Long teacherId, Long courseId);
}

