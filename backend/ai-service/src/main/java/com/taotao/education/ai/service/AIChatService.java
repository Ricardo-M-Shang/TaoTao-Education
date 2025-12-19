package com.taotao.education.ai.service;

import com.taotao.education.ai.dto.AIChatRequestDTO;
import com.taotao.education.ai.vo.AIChatResponseVO;

/**
 * AI助手对话服务
 */
public interface AIChatService {
    
    /**
     * 与AI助手对话
     *
     * @param userId 用户ID
     * @param request 对话请求
     * @return AI回复
     */
    AIChatResponseVO chat(Long userId, AIChatRequestDTO request);
}
