package com.taotao.education.course.controller;

import com.taotao.education.common.result.Result;
import com.taotao.education.course.service.CategoryService;
import com.taotao.education.course.vo.CategoryTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "课程分类", description = "课程分类管理接口")
@RestController
@RequestMapping("/api/course/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }
}

