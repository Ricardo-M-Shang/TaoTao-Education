package com.taotao.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.course.entity.Category;
import com.taotao.education.course.mapper.CategoryMapper;
import com.taotao.education.course.service.CategoryService;
import com.taotao.education.course.vo.CategoryTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        // 获取所有启用的分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getSort);
        List<Category> allCategories = this.list(wrapper);

        // 构建树形结构
        return buildTree(allCategories, 0L);
    }

    /**
     * 递归构建分类树
     */
    private List<CategoryTreeVO> buildTree(List<Category> categories, Long parentId) {
        return categories.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .map(c -> {
                    CategoryTreeVO vo = new CategoryTreeVO();
                    BeanUtils.copyProperties(c, vo);
                    vo.setChildren(buildTree(categories, c.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> getByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId)
               .eq(Category::getStatus, 1)
               .orderByAsc(Category::getSort);
        return this.list(wrapper);
    }
}

