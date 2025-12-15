package com.taotao.education.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.course.dto.ReviewCreateDTO;
import com.taotao.education.course.entity.CourseReview;
import com.taotao.education.course.vo.CourseReviewVO;

/**
 * 课程评价服务接口
 */
public interface CourseReviewService extends IService<CourseReview> {

    /**
     * 添加评价
     */
    void addReview(Long userId, String username, String nickname, String avatar, ReviewCreateDTO dto);

    /**
     * 获取课程评价列表
     */
    Page<CourseReviewVO> getReviewsByCourse(Long courseId, Integer pageNum, Integer pageSize);

    /**
     * 检查用户是否已评价
     */
    boolean hasReviewed(Long userId, Long courseId);

    /**
     * 删除评价
     */
    void deleteReview(Long userId, Long reviewId);
}

