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
     * 下架课程
     */
    void offlineCourse(Long courseId);
}

