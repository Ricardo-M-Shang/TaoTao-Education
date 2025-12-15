package com.taotao.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.common.result.ResultCode;
import com.taotao.education.course.dto.AnswerCreateDTO;
import com.taotao.education.course.dto.QuestionCreateDTO;
import com.taotao.education.course.entity.CourseAnswer;
import com.taotao.education.course.entity.Course;
import com.taotao.education.course.entity.CourseQuestion;
import com.taotao.education.course.mapper.CourseAnswerMapper;
import com.taotao.education.course.mapper.CourseQuestionMapper;
import com.taotao.education.course.mapper.CourseMapper;
import com.taotao.education.course.service.CourseQaService;
import com.taotao.education.course.vo.AnswerVO;
import com.taotao.education.course.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseQaServiceImpl extends ServiceImpl<CourseQuestionMapper, CourseQuestion> implements CourseQaService {

    private final CourseMapper courseMapper;
    private final CourseAnswerMapper courseAnswerMapper;

    @Override
    public void addQuestion(Long userId, String username, String nickname, QuestionCreateDTO dto) {
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        CourseQuestion question = new CourseQuestion();
        question.setCourseId(dto.getCourseId());
        question.setUserId(userId);
        question.setUsername(username);
        question.setNickname(nickname != null ? nickname : username);
        question.setContent(dto.getContent());
        this.save(question);
    }

    @Override
    public void addAnswer(Long userId, String username, String nickname, AnswerCreateDTO dto) {
        CourseQuestion question = this.getById(dto.getQuestionId());
        if (question == null || !question.getCourseId().equals(dto.getCourseId())) {
            throw new BusinessException("问题不存在或不属于该课程");
        }
        CourseAnswer answer = new CourseAnswer();
        answer.setQuestionId(dto.getQuestionId());
        answer.setCourseId(dto.getCourseId());
        answer.setUserId(userId);
        answer.setUsername(username);
        answer.setNickname(nickname != null ? nickname : username);
        answer.setContent(dto.getContent());
        answer.setAccepted(0);
        courseAnswerMapper.insert(answer);
    }

    @Override
    public Page<QuestionVO> listQuestions(Long courseId, Integer pageNum, Integer pageSize) {
        Page<CourseQuestion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CourseQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseQuestion::getCourseId, courseId)
                .orderByDesc(CourseQuestion::getCreateTime);
        Page<CourseQuestion> result = this.page(page, wrapper);

        List<Long> questionIds = result.getRecords().stream().map(CourseQuestion::getId).collect(Collectors.toList());
        final Map<Long, List<AnswerVO>> answerMap;
        if (!CollectionUtils.isEmpty(questionIds)) {
            LambdaQueryWrapper<CourseAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.in(CourseAnswer::getQuestionId, questionIds)
                    .orderByAsc(CourseAnswer::getCreateTime);
            List<CourseAnswer> answers = courseAnswerMapper.selectList(answerWrapper);
            answerMap = answers.stream()
                    .map(this::convertToAnswerVO)
                    .collect(Collectors.groupingBy(AnswerVO::getQuestionId));
        } else {
            answerMap = Collections.emptyMap();
        }

        Page<QuestionVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<QuestionVO> voList = result.getRecords().stream().map(q -> {
            QuestionVO vo = new QuestionVO();
            BeanUtils.copyProperties(q, vo);
            List<AnswerVO> list = answerMap.getOrDefault(q.getId(), Collections.emptyList());
            vo.setAnswers(list);
            vo.setAnswerCount(list.size());
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void acceptAnswer(Long userId, Long answerId) {
        CourseAnswer answer = courseAnswerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException("回答不存在");
        }
        CourseQuestion question = this.getById(answer.getQuestionId());
        if (question == null) {
            throw new BusinessException("问题不存在");
        }
        if (!Objects.equals(question.getUserId(), userId)) {
            throw new BusinessException("只有提问者可以采纳回答");
        }
        // 先将该问题下已采纳的置为未采纳
        LambdaQueryWrapper<CourseAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseAnswer::getQuestionId, question.getId())
                .eq(CourseAnswer::getAccepted, 1);
        List<CourseAnswer> accepted = courseAnswerMapper.selectList(wrapper);
        for (CourseAnswer a : accepted) {
            a.setAccepted(0);
            courseAnswerMapper.updateById(a);
        }
        answer.setAccepted(1);
        courseAnswerMapper.updateById(answer);
    }

    private AnswerVO convertToAnswerVO(CourseAnswer answer) {
        AnswerVO vo = new AnswerVO();
        BeanUtils.copyProperties(answer, vo);
        return vo;
    }
}


