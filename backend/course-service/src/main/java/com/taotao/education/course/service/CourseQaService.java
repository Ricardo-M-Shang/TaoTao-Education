package com.taotao.education.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.course.dto.AnswerCreateDTO;
import com.taotao.education.course.dto.QuestionCreateDTO;
import com.taotao.education.course.entity.CourseQuestion;
import com.taotao.education.course.vo.QuestionVO;

public interface CourseQaService extends IService<CourseQuestion> {

    void addQuestion(Long userId, String username, String nickname, QuestionCreateDTO dto);

    void addAnswer(Long userId, String username, String nickname, AnswerCreateDTO dto);

    Page<QuestionVO> listQuestions(Long courseId, Integer pageNum, Integer pageSize);

    void acceptAnswer(Long userId, Long answerId);
}


