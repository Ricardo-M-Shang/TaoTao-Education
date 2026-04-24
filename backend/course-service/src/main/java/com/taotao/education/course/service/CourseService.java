package com.taotao.education.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.course.dto.CourseCreateDTO;
import com.taotao.education.course.dto.CourseQueryDTO;
import com.taotao.education.course.dto.CourseUpdateDTO;
import com.taotao.education.course.entity.Course;
import com.taotao.education.course.vo.CourseDetailVO;
import com.taotao.education.course.vo.CourseListVO;

/**
 * 课程服务接口
 */
public interface CourseService extends IService<Course> {

    /**
     * 分页查询课程列表
     */
    Page<CourseListVO> pageList(CourseQueryDTO queryDTO);

    /**
     * 获取课程详情
     */
    CourseDetailVO getDetail(Long courseId);

    /**
     * 创建课程
     */
    Long createCourse(Long teacherId, CourseCreateDTO createDTO);

    /**
     * 更新课程
     */
    void updateCourse(Long courseId, CourseUpdateDTO updateDTO);

    /**
     * 发布课程
     */
    void publishCourse(Long courseId);

    /**
     * 下架课程（讲师、机构均可调用）
     */
    void offlineCourse(Long courseId);

    /**
     * 讲师分页查询自己创建的课程（含草稿/发布状态）
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.taotao.education.course.vo.CourseListVO> pageTeacherCourses(Long teacherId, com.taotao.education.course.dto.CourseQueryDTO queryDTO);
    
    /**
     * 讲师更新课程状态（发布/下架）
     */
    void updateCourseStatus(Long teacherId, Long courseId, Integer status);

    /**
     * 讲师：创建章节
     */
    Long createChapter(Long teacherId, com.taotao.education.course.dto.ChapterCreateDTO dto);

    /**
     * 讲师：更新章节
     */
    void updateChapter(Long teacherId, Long chapterId, com.taotao.education.course.dto.ChapterUpdateDTO dto);

    /**
     * 讲师：删除章节
     */
    void deleteChapter(Long teacherId, Long chapterId);

    /**
     * 讲师：创建课时（仅录播/图文）
     */
    Long createLesson(Long teacherId, com.taotao.education.course.dto.LessonCreateDTO dto);

    /**
     * 讲师：更新课时
     */
    void updateLesson(Long teacherId, Long lessonId, com.taotao.education.course.dto.LessonUpdateDTO dto);

    /**
     * 讲师：删除课时
     */
    void deleteLesson(Long teacherId, Long lessonId);

    /**
     * 机构：分页查询课程（可筛选待审核/已上架/下架）
     */
    Page<CourseListVO> pageOrgCourses(Long orgId, CourseQueryDTO queryDTO);

    /**
     * 机构：审核通过课程
     */
    void approveCourse(Long orgId, Long auditorId, String auditorName, Long courseId, String remark);

    /**
     * 机构：审核拒绝课程（退回草稿）
     */
    void rejectCourse(Long orgId, Long auditorId, String auditorName, Long courseId, String remark);

    /**
     * 机构：下架课程（需同机构）
     */
    void offlineCourseByOrg(Long orgId, Long courseId);

    /**
     * 机构下讲师列表
     */
    java.util.List<com.taotao.education.course.vo.TeacherOptionVO> listOrgTeachers(Long orgId);

    /**
     * 运营端课程概览
     */
    com.taotao.education.course.vo.OpsCourseOverviewVO getOpsOverview();

    /**
     * 增加课程学习人数（内部接口）
     */
    void increaseStudyCount(Long courseId, Integer count);
}

