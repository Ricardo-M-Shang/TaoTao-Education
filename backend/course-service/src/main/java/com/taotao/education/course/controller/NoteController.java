package com.taotao.education.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taotao.education.common.result.Result;
import com.taotao.education.course.dto.NoteSaveDTO;
import com.taotao.education.course.service.NoteService;
import com.taotao.education.course.vo.NoteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 笔记控制器
 */
@Tag(name = "学习笔记", description = "学习笔记相关接口")
@RestController
@RequestMapping("/api/course/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "获取课程笔记列表")
    @GetMapping("/list/{courseId}")
    public Result<Page<NoteVO>> getCourseNotes(@RequestHeader("X-User-Id") Long userId,
                                               @PathVariable Long courseId,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<NoteVO> page = noteService.getCourseNotes(userId, courseId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取课时笔记列表")
    @GetMapping("/lesson/{lessonId}")
    public Result<List<NoteVO>> getLessonNotes(@RequestHeader("X-User-Id") Long userId,
                                               @PathVariable Long lessonId) {
        List<NoteVO> notes = noteService.getLessonNotes(userId, lessonId);
        return Result.success(notes);
    }

    @Operation(summary = "保存笔记")
    @PostMapping("/save")
    public Result<Long> saveNote(@RequestHeader("X-User-Id") Long userId,
                                 @Valid @RequestBody NoteSaveDTO dto) {
        Long noteId = noteService.saveNote(userId, dto);
        return Result.success(noteId);
    }

    @Operation(summary = "更新笔记")
    @PutMapping("/update/{noteId}")
    public Result<Void> updateNote(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long noteId,
                                   @RequestBody Map<String, String> body) {
        String content = body.get("content");
        noteService.updateNote(userId, noteId, content);
        return Result.success();
    }

    @Operation(summary = "删除笔记")
    @DeleteMapping("/{noteId}")
    public Result<Void> deleteNote(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long noteId) {
        noteService.deleteNote(userId, noteId);
        return Result.success();
    }
}

