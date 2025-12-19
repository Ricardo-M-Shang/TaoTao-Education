package com.taotao.education.ai.service;

import com.taotao.education.ai.dto.RecommendRequestDTO;
import com.taotao.education.ai.vo.RecommendResultVO;

/**
 * 课程推荐服务接口
 */
public interface RecommendService {

    /**
     * 获取个性化课程推荐
     *
     * @param userId  用户ID
     * @param request 推荐请求参数
     * @return 推荐结果
     */
    RecommendResultVO getRecommendations(Long userId, RecommendRequestDTO request);

    /**
     * 刷新推荐（清除缓存后重新生成）
     *
     * @param userId  用户ID
     * @param request 推荐请求参数
     * @return 推荐结果
     */
    RecommendResultVO refreshRecommendations(Long userId, RecommendRequestDTO request);

    /**
     * 获取热门课程推荐（无需登录）
     *
     * @param count 数量
     * @return 推荐结果
     */
    RecommendResultVO getPopularRecommendations(int count);
}

