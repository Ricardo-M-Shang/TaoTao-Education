package com.taotao.education.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.course.entity.Note;
import com.taotao.education.course.vo.NoteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 笔记Mapper
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    /**
     * 获取课时笔记（带课时标题）
     */
    @Select("""
        SELECT n.id, n.course_id, n.lesson_id, l.title as lesson_title,
               n.content, n.video_time, n.create_time, n.update_time
        FROM t_note n
        LEFT JOIN t_lesson l ON n.lesson_id = l.id
        WHERE n.user_id = #{userId} AND n.lesson_id = #{lessonId}
        ORDER BY n.create_time DESC
        """)
    List<NoteVO> getLessonNotes(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    /**
     * 获取课程所有笔记
     */
    @Select("""
        SELECT n.id, n.course_id, n.lesson_id, l.title as lesson_title,
               n.content, n.video_time, n.create_time, n.update_time
        FROM t_note n
        LEFT JOIN t_lesson l ON n.lesson_id = l.id
        WHERE n.user_id = #{userId} AND n.course_id = #{courseId}
        ORDER BY n.create_time DESC
        """)
    List<NoteVO> getCourseNotes(@Param("userId") Long userId, @Param("courseId") Long courseId);
}

