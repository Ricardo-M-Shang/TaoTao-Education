package com.taotao.education.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.course.entity.Lesson;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课时Mapper接口
 */
@Mapper
public interface LessonMapper extends BaseMapper<Lesson> {
}

