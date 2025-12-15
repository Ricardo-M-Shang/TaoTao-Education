package com.taotao.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.course.dto.ReviewCreateDTO;
import com.taotao.education.course.entity.Course;
import com.taotao.education.course.entity.CourseReview;
import com.taotao.education.course.mapper.CourseMapper;
import com.taotao.education.course.mapper.CourseReviewMapper;
import com.taotao.education.course.service.CourseReviewService;
import com.taotao.education.course.vo.CourseReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程评价服务实现类
 */
@Service
@RequiredArgsConstructor
public class CourseReviewServiceImpl extends ServiceImpl<CourseReviewMapper, CourseReview> implements CourseReviewService {

    private final CourseMapper courseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReview(Long userId, String username, String nickname, String avatar, ReviewCreateDTO dto) {
        // 检查是否已评价
        if (hasReviewed(userId, dto.getCourseId())) {
            throw new BusinessException("您已经评价过该课程");
        }

        // 创建评价
        CourseReview review = new CourseReview();
        review.setCourseId(dto.getCourseId());
        review.setUserId(userId);
        review.setUsername(username);
        review.setNickname(nickname);
        review.setAvatar(avatar);
        review.setScore(dto.getScore());
        review.setContent(dto.getContent());
        review.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);

        this.save(review);

        // 更新课程平均评分
        updateCourseScore(dto.getCourseId());
    }

    @Override
    public Page<CourseReviewVO> getReviewsByCourse(Long courseId, Integer pageNum, Integer pageSize) {
        Page<CourseReview> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<CourseReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseReview::getCourseId, courseId)
               .orderByDesc(CourseReview::getCreateTime);

        Page<CourseReview> result = this.page(page, wrapper);

        Page<CourseReviewVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<CourseReviewVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public boolean hasReviewed(Long userId, Long courseId) {
        LambdaQueryWrapper<CourseReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseReview::getUserId, userId)
               .eq(CourseReview::getCourseId, courseId);
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long userId, Long reviewId) {
        CourseReview review = this.getById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该评价");
        }

        Long courseId = review.getCourseId();
        this.removeById(reviewId);

        // 更新课程平均评分
        updateCourseScore(courseId);
    }

    /**
     * 更新课程平均评分
     */
    private void updateCourseScore(Long courseId) {
        LambdaQueryWrapper<CourseReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseReview::getCourseId, courseId);
        List<CourseReview> reviews = this.list(wrapper);

        if (reviews.isEmpty()) {
            return;
        }

        double avgScore = reviews.stream()
                .mapToInt(CourseReview::getScore)
                .average()
                .orElse(0.0);

        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            course.setScore(BigDecimal.valueOf(avgScore).setScale(1, RoundingMode.HALF_UP));
            courseMapper.updateById(course);
        }
    }

    private CourseReviewVO convertToVO(CourseReview review) {
        CourseReviewVO vo = new CourseReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 匿名处理
        if (review.getIsAnonymous() != null && review.getIsAnonymous() == 1) {
            vo.setNickname("匿名用户");
            vo.setAvatar(null);
        }

        return vo;
    }
}

