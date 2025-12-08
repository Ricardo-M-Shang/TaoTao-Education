package com.taotao.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.common.result.ResultCode;
import com.taotao.education.course.dto.CourseCreateDTO;
import com.taotao.education.course.dto.CourseQueryDTO;
import com.taotao.education.course.dto.CourseUpdateDTO;
import com.taotao.education.course.entity.Category;
import com.taotao.education.course.entity.Chapter;
import com.taotao.education.course.entity.Course;
import com.taotao.education.course.entity.Lesson;
import com.taotao.education.course.mapper.CategoryMapper;
import com.taotao.education.course.mapper.ChapterMapper;
import com.taotao.education.course.mapper.CourseMapper;
import com.taotao.education.course.mapper.LessonMapper;
import com.taotao.education.course.service.CourseService;
import com.taotao.education.course.vo.ChapterVO;
import com.taotao.education.course.vo.CourseDetailVO;
import com.taotao.education.course.vo.CourseListVO;
import com.taotao.education.course.vo.LessonVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CategoryMapper categoryMapper;
    private final ChapterMapper chapterMapper;
    private final LessonMapper lessonMapper;

    @Override
    public Page<CourseListVO> pageList(CourseQueryDTO queryDTO) {
        Page<Course> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(Course::getTitle, queryDTO.getKeyword())
                   .or()
                   .like(Course::getDescription, queryDTO.getKeyword());
        }
        
        // 分类筛选
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Course::getCategoryId, queryDTO.getCategoryId());
        }
        
        // 类型筛选
        if (queryDTO.getType() != null) {
            wrapper.eq(Course::getType, queryDTO.getType());
        }
        
        // 是否免费
        if (queryDTO.getIsFree() != null) {
            wrapper.eq(Course::getIsFree, queryDTO.getIsFree());
        }
        
        // 只查询已发布的课程
        wrapper.eq(Course::getStatus, 2);
        
        // 排序
        if ("popular".equals(queryDTO.getOrderBy())) {
            wrapper.orderByDesc(Course::getStudyCount);
        } else if ("newest".equals(queryDTO.getOrderBy())) {
            wrapper.orderByDesc(Course::getCreateTime);
        } else if ("price".equals(queryDTO.getOrderBy())) {
            wrapper.orderByAsc(Course::getPrice);
        } else {
            wrapper.orderByDesc(Course::getSort);
        }

        Page<Course> result = this.page(page, wrapper);

        // 转换为VO
        Page<CourseListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<CourseListVO> voList = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public CourseDetailVO getDetail(Long courseId) {
        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        CourseDetailVO vo = new CourseDetailVO();
        BeanUtils.copyProperties(course, vo);

        // 获取章节和课时
        LambdaQueryWrapper<Chapter> chapterWrapper = new LambdaQueryWrapper<>();
        chapterWrapper.eq(Chapter::getCourseId, courseId)
                      .orderByAsc(Chapter::getSort);
        List<Chapter> chapters = chapterMapper.selectList(chapterWrapper);

        List<ChapterVO> chapterVOs = chapters.stream().map(chapter -> {
            ChapterVO chapterVO = new ChapterVO();
            BeanUtils.copyProperties(chapter, chapterVO);

            // 获取章节下的课时
            LambdaQueryWrapper<Lesson> lessonWrapper = new LambdaQueryWrapper<>();
            lessonWrapper.eq(Lesson::getChapterId, chapter.getId())
                         .orderByAsc(Lesson::getSort);
            List<Lesson> lessons = lessonMapper.selectList(lessonWrapper);

            List<LessonVO> lessonVOs = lessons.stream().map(lesson -> {
                LessonVO lessonVO = new LessonVO();
                BeanUtils.copyProperties(lesson, lessonVO);
                return lessonVO;
            }).collect(Collectors.toList());

            chapterVO.setLessons(lessonVOs);
            return chapterVO;
        }).collect(Collectors.toList());

        vo.setChapters(chapterVOs);
        return vo;
    }

    @Override
    public Long createCourse(Long teacherId, CourseCreateDTO createDTO) {
        Course course = new Course();
        BeanUtils.copyProperties(createDTO, course);
        course.setTeacherId(teacherId);
        course.setStatus(0); // 草稿状态
        course.setStudyCount(0);
        course.setLessonCount(0);

        // 获取分类名称
        if (createDTO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(createDTO.getCategoryId());
            if (category != null) {
                course.setCategoryName(category.getName());
            }
        }

        this.save(course);
        return course.getId();
    }

    @Override
    public void updateCourse(Long courseId, CourseUpdateDTO updateDTO) {
        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        BeanUtils.copyProperties(updateDTO, course);

        // 更新分类名称
        if (updateDTO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(updateDTO.getCategoryId());
            if (category != null) {
                course.setCategoryName(category.getName());
            }
        }

        this.updateById(course);
    }

    @Override
    public void publishCourse(Long courseId) {
        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        course.setStatus(2); // 已发布
        this.updateById(course);
    }

    @Override
    public void offlineCourse(Long courseId) {
        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        course.setStatus(3); // 已下架
        this.updateById(course);
    }

    private CourseListVO convertToListVO(Course course) {
        CourseListVO vo = new CourseListVO();
        BeanUtils.copyProperties(course, vo);
        return vo;
    }
}

