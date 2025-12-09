package com.taotao.education.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.course.entity.CourseFavorite;
import com.taotao.education.course.vo.CourseFavoriteVO;

/**
 * 课程收藏服务接口
 */
public interface CourseFavoriteService extends IService<CourseFavorite> {

    /**
     * 添加收藏
     */
    void addFavorite(Long userId, Long courseId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long userId, Long courseId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorite(Long userId, Long courseId);

    /**
     * 获取用户收藏列表
     */
    Page<CourseFavoriteVO> getUserFavorites(Long userId, Integer pageNum, Integer pageSize);
}

