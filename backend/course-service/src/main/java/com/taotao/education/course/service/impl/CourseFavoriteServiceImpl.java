package com.taotao.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.course.entity.Course;
import com.taotao.education.course.entity.CourseFavorite;
import com.taotao.education.course.mapper.CourseFavoriteMapper;
import com.taotao.education.course.mapper.CourseMapper;
import com.taotao.education.course.service.CourseFavoriteService;
import com.taotao.education.course.vo.CourseFavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程收藏服务实现类
 */
@Service
@RequiredArgsConstructor
public class CourseFavoriteServiceImpl extends ServiceImpl<CourseFavoriteMapper, CourseFavorite> implements CourseFavoriteService {

    private final CourseMapper courseMapper;

    @Override
    public void addFavorite(Long userId, Long courseId) {
        if (isFavorite(userId, courseId)) {
            throw new BusinessException("已收藏该课程");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        CourseFavorite favorite = new CourseFavorite();
        favorite.setUserId(userId);
        favorite.setCourseId(courseId);
        this.save(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long courseId) {
        LambdaQueryWrapper<CourseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseFavorite::getUserId, userId)
               .eq(CourseFavorite::getCourseId, courseId);
        this.remove(wrapper);
    }

    @Override
    public boolean isFavorite(Long userId, Long courseId) {
        LambdaQueryWrapper<CourseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseFavorite::getUserId, userId)
               .eq(CourseFavorite::getCourseId, courseId);
        return this.count(wrapper) > 0;
    }

    @Override
    public Page<CourseFavoriteVO> getUserFavorites(Long userId, Integer pageNum, Integer pageSize) {
        Page<CourseFavorite> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<CourseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseFavorite::getUserId, userId)
               .orderByDesc(CourseFavorite::getCreateTime);

        Page<CourseFavorite> result = this.page(page, wrapper);

        Page<CourseFavoriteVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<CourseFavoriteVO> voList = new ArrayList<>();

        for (CourseFavorite favorite : result.getRecords()) {
            CourseFavoriteVO vo = new CourseFavoriteVO();
            vo.setId(favorite.getId());
            vo.setCourseId(favorite.getCourseId());
            vo.setCreateTime(favorite.getCreateTime());

            // 获取课程信息
            Course course = courseMapper.selectById(favorite.getCourseId());
            if (course != null) {
                vo.setCourseTitle(course.getTitle());
                vo.setCourseCover(course.getCover());
                vo.setTeacherName(course.getTeacherName());
                vo.setPrice(course.getPrice());
                vo.setIsFree(course.getIsFree());
                vo.setStudyCount(course.getStudyCount());
                vo.setScore(course.getScore());
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }
}

