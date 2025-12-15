package com.taotao.education.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.course.dto.NoteSaveDTO;
import com.taotao.education.course.entity.Note;
import com.taotao.education.course.vo.NoteVO;

import java.util.List;

/**
 * 笔记服务接口
 */
public interface NoteService extends IService<Note> {

    /**
     * 获取课程笔记列表（分页）
     */
    Page<NoteVO> getCourseNotes(Long userId, Long courseId, int pageNum, int pageSize);

    /**
     * 获取课时笔记列表
     */
    List<NoteVO> getLessonNotes(Long userId, Long lessonId);

    /**
     * 保存笔记
     */
    Long saveNote(Long userId, NoteSaveDTO dto);

    /**
     * 更新笔记
     */
    void updateNote(Long userId, Long noteId, String content);

    /**
     * 删除笔记
     */
    void deleteNote(Long userId, Long noteId);
}

