package com.taotao.education.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.order.entity.UserCourse;
import com.taotao.education.order.vo.UserCourseVO;

import java.util.List;

/**
 * 用户课程服务接口
 */
public interface UserCourseService extends IService<UserCourse> {

    /**
     * 获取用户已购课程列表
     */
    List<UserCourseVO> getUserCourses(Long userId);

    /**
     * 更新学习进度
     */
    void updateProgress(Long userId, Long courseId, Integer progress);
}

