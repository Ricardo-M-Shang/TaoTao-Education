package com.taotao.education.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.order.entity.Order;
import com.taotao.education.order.entity.UserCourse;
import com.taotao.education.order.mapper.OrderMapper;
import com.taotao.education.order.mapper.UserCourseMapper;
import com.taotao.education.order.service.UserCourseService;
import com.taotao.education.order.vo.UserCourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户课程服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserCourseServiceImpl extends ServiceImpl<UserCourseMapper, UserCourse> implements UserCourseService {

    private final OrderMapper orderMapper;

    @Override
    public List<UserCourseVO> getUserCourses(Long userId) {
        // 查询用户已购课程
        LambdaQueryWrapper<UserCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCourse::getUserId, userId)
               .orderByDesc(UserCourse::getCreateTime);
        List<UserCourse> userCourses = this.list(wrapper);

        List<UserCourseVO> result = new ArrayList<>();
        for (UserCourse uc : userCourses) {
            UserCourseVO vo = new UserCourseVO();
            vo.setId(uc.getId());
            vo.setCourseId(uc.getCourseId());
            vo.setProgress(uc.getProgress() != null ? uc.getProgress() : 0);
            vo.setLastStudyTime(uc.getLastStudyTime());
            vo.setIsFinished(uc.getIsFinished());
            vo.setCreateTime(uc.getCreateTime());

            // 从订单中获取课程信息
            if (uc.getOrderId() != null) {
                Order order = orderMapper.selectById(uc.getOrderId());
                if (order != null) {
                    vo.setCourseTitle(order.getCourseTitle());
                    vo.setCourseCover(order.getCourseCover());
                    vo.setTeacherName(order.getTeacherName());
                }
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public void updateProgress(Long userId, Long courseId, Integer progress) {
        LambdaQueryWrapper<UserCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCourse::getUserId, userId)
               .eq(UserCourse::getCourseId, courseId);
        UserCourse userCourse = this.getOne(wrapper);

        if (userCourse != null) {
            userCourse.setProgress(progress);
            userCourse.setLastStudyTime(LocalDateTime.now());
            if (progress >= 100) {
                userCourse.setIsFinished(1);
            }
            this.updateById(userCourse);
        }
    }

    @Override
    public List<com.taotao.education.order.vo.TeacherStudentVO> listStudentsByCourse(Long teacherId, Long courseId) {
        return baseMapper.listStudentsByCourse(teacherId, courseId);
    }
}

