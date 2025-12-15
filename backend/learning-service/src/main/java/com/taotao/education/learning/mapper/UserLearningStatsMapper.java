package com.taotao.education.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taotao.education.learning.entity.UserLearningStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户学习总统计Mapper
 */
@Mapper
public interface UserLearningStatsMapper extends BaseMapper<UserLearningStats> {
}
