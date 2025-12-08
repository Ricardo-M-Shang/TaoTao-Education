package com.taotao.education.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.course.entity.Category;
import com.taotao.education.course.vo.CategoryTreeVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树
     */
    List<CategoryTreeVO> getCategoryTree();

    /**
     * 根据父ID获取子分类
     */
    List<Category> getByParentId(Long parentId);
}

