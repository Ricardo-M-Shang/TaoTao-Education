package com.taotao.education.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.course.dto.AnswerCreateDTO;
import com.taotao.education.course.dto.QuestionCreateDTO;
import com.taotao.education.course.service.CourseQaService;
import com.taotao.education.course.vo.QuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程问答", description = "课程问答接口")
@RestController
@RequestMapping("/api/course/qa")
@RequiredArgsConstructor
public class CourseQaController {

    private final CourseQaService qaService;

    @Operation(summary = "提问")
    @PostMapping("/question")
    public Result<Void> addQuestion(@RequestHeader("X-User-Id") Long userId,
                                    @RequestHeader(value = "X-Username", required = false) String username,
                                    @RequestHeader(value = "X-Nickname", required = false) String nickname,
                                    @Valid @RequestBody QuestionCreateDTO dto) {
        qaService.addQuestion(userId, username, nickname, dto);
        return Result.success();
    }

    @Operation(summary = "回答")
    @PostMapping("/answer")
    public Result<Void> addAnswer(@RequestHeader("X-User-Id") Long userId,
                                  @RequestHeader(value = "X-Username", required = false) String username,
                                  @RequestHeader(value = "X-Nickname", required = false) String nickname,
                                  @Valid @RequestBody AnswerCreateDTO dto) {
        qaService.addAnswer(userId, username, nickname, dto);
        return Result.success();
    }

    @Operation(summary = "问题列表（含回答）")
    @GetMapping("/question/{courseId}")
    public Result<Page<QuestionVO>> listQuestions(@PathVariable Long courseId,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<QuestionVO> page = qaService.listQuestions(courseId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "采纳回答")
    @PostMapping("/answer/{answerId}/accept")
    public Result<Void> acceptAnswer(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable Long answerId) {
        qaService.acceptAnswer(userId, answerId);
        return Result.success();
    }
}


