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
import com.taotao.education.course.dto.ChapterCreateDTO;
import com.taotao.education.course.dto.ChapterUpdateDTO;
import com.taotao.education.course.dto.LessonCreateDTO;
import com.taotao.education.course.dto.LessonUpdateDTO;
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

    @Override
    public Page<CourseListVO> pageTeacherCourses(Long teacherId, CourseQueryDTO queryDTO) {
        Page<Course> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, teacherId);

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

        // 状态筛选（可选：0草稿 2已发布 3已下架）
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Course::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(Course::getUpdateTime);

        Page<Course> result = this.page(page, wrapper);
        Page<CourseListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<CourseListVO> voList = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void updateCourseStatus(Long teacherId, Long courseId, Integer status) {
        Course course = this.getById(courseId);
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        // 仅允许：草稿/已下架 -> 发布(2)，已发布(2) -> 下架(3)
        if (status == 2) {
            if (course.getStatus() != null && course.getStatus() == 2) {
                throw new BusinessException("课程已是发布状态");
            }
        } else if (status == 3) {
            if (course.getStatus() == null || course.getStatus() != 2) {
                throw new BusinessException("仅已发布课程可下架");
            }
        }
        course.setStatus(status);
        this.updateById(course);
    }

    @Override
    public Long createChapter(Long teacherId, ChapterCreateDTO dto) {
        Course course = this.getById(dto.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        Chapter chapter = new Chapter();
        BeanUtils.copyProperties(dto, chapter);
        chapterMapper.insert(chapter);
        return chapter.getId();
    }

    @Override
    public void updateChapter(Long teacherId, Long chapterId, ChapterUpdateDTO dto) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        Course course = this.getById(chapter.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        chapter.setTitle(dto.getTitle());
        chapter.setSort(dto.getSort());
        chapterMapper.updateById(chapter);
    }

    @Override
    public void deleteChapter(Long teacherId, Long chapterId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) return;
        Course course = this.getById(chapter.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        // 删除章节下课时
        LambdaQueryWrapper<Lesson> lw = new LambdaQueryWrapper<>();
        lw.eq(Lesson::getChapterId, chapterId);
        lessonMapper.delete(lw);
        chapterMapper.deleteById(chapterId);
    }

    @Override
    public Long createLesson(Long teacherId, LessonCreateDTO dto) {
        Course course = this.getById(dto.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        if (course.getType() != 1 && dto.getType() == 1) {
            throw new BusinessException("当前课程非录播，无法创建视频课时");
        }
        Lesson lesson = new Lesson();
        BeanUtils.copyProperties(dto, lesson);
        lessonMapper.insert(lesson);
        // 更新课时数
        course.setLessonCount((course.getLessonCount() == null ? 0 : course.getLessonCount()) + 1);
        this.updateById(course);
        return lesson.getId();
    }

    @Override
    public void updateLesson(Long teacherId, Long lessonId, LessonUpdateDTO dto) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        Course course = this.getById(lesson.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        lesson.setTitle(dto.getTitle());
        lesson.setVideoUrl(dto.getVideoUrl());
        lesson.setDuration(dto.getDuration());
        lesson.setIsFree(dto.getIsFree());
        lesson.setSort(dto.getSort());
        lesson.setType(dto.getType());
        lessonMapper.updateById(lesson);
    }

    @Override
    public void deleteLesson(Long teacherId, Long lessonId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) return;
        Course course = this.getById(lesson.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        lessonMapper.deleteById(lessonId);
        // 课时数更新
        Long cnt = lessonMapper.selectCount(new LambdaQueryWrapper<Lesson>().eq(Lesson::getCourseId, course.getId()));
        course.setLessonCount(cnt == null ? 0 : cnt.intValue());
        this.updateById(course);
    }

    private CourseListVO convertToListVO(Course course) {
        CourseListVO vo = new CourseListVO();
        BeanUtils.copyProperties(course, vo);
        vo.setStatus(course.getStatus());
        return vo;
    }
}

