package com.taotao.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.course.dto.NoteSaveDTO;
import com.taotao.education.course.entity.Note;
import com.taotao.education.course.mapper.NoteMapper;
import com.taotao.education.course.service.NoteService;
import com.taotao.education.course.vo.NoteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记服务实现类
 */
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Override
    public Page<NoteVO> getCourseNotes(Long userId, Long courseId, int pageNum, int pageSize) {
        // 先查询总数
        LambdaQueryWrapper<Note> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Note::getUserId, userId)
                    .eq(Note::getCourseId, courseId);
        long total = this.count(countWrapper);

        // 分页查询
        Page<NoteVO> page = new Page<>(pageNum, pageSize, total);
        
        if (total > 0) {
            List<NoteVO> notes = baseMapper.getCourseNotes(userId, courseId);
            // 手动分页
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, notes.size());
            if (start < notes.size()) {
                page.setRecords(notes.subList(start, end));
            }
        }
        
        return page;
    }

    @Override
    public List<NoteVO> getLessonNotes(Long userId, Long lessonId) {
        return baseMapper.getLessonNotes(userId, lessonId);
    }

    @Override
    public Long saveNote(Long userId, NoteSaveDTO dto) {
        Note note = new Note();
        note.setUserId(userId);
        note.setCourseId(dto.getCourseId());
        note.setLessonId(dto.getLessonId());
        note.setContent(dto.getContent());
        note.setVideoTime(dto.getVideoTime());
        
        this.save(note);
        return note.getId();
    }

    @Override
    public void updateNote(Long userId, Long noteId, String content) {
        Note note = this.getById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此笔记");
        }
        
        note.setContent(content);
        this.updateById(note);
    }

    @Override
    public void deleteNote(Long userId, Long noteId) {
        Note note = this.getById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此笔记");
        }
        
        this.removeById(noteId);
    }
}

