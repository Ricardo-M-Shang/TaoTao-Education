package com.taotao.education.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.learning.entity.UserLearningStats;

/**
 * 用户学习总统计服务接口
 */
public interface UserLearningStatsService extends IService<UserLearningStats> {

    /**
     * 获取用户学习统计
     */
    UserLearningStats getUserStats(Long userId);

    /**
     * 更新用户学习统计
     */
    void updateUserStats(Long userId);

    /**
     * 更新用户连续学习天数
     */
    void updateUserStreak(Long userId);
}
