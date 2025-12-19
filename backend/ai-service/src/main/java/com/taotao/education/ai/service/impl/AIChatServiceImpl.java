package com.taotao.education.ai.service.impl;

import com.taotao.education.ai.client.DeepSeekClient;
import com.taotao.education.ai.dto.AIChatRequestDTO;
import com.taotao.education.ai.service.AIChatService;
import com.taotao.education.ai.vo.AIChatResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private final DeepSeekClient deepSeekClient;

    private static final String STUDENT_SYSTEM_PROMPT = """
            你是一个在线教育平台的智能学习助手。你的名字叫“AI学习助手”。
            
            你的角色设定：
            1. 语气亲切、活泼、充满鼓励（可以使用少量Emoji，但不要过多）。
            2. 主要职责是帮助学生解答学习困惑、制定学习计划、提供缓解压力的建议。
            3. 如果学生问及非学习相关的问题，请礼貌地引导回学习话题。
            4. 回答要简洁明了，重点突出。
            
            请用中文回答。
            """;

    private static final String TEACHER_SYSTEM_PROMPT = """
            你是一个在线教育平台的专业教学助手。你的名字叫“AI教学助手”。
            
            你的角色设定：
            1. 语气专业、稳重、细致、富有同理心。
            2. 你的对话对象是“老师”，请务必称呼对方为“老师”或“您”，绝对不要称呼对方为“同学”或“学生”。
            3. 主要职责是协助老师进行备课、提供教学灵感、解答教学管理问题、关注老师的身心健康。
            4. 如果老师询问教学相关问题，请提供结构化、可落地的建议。
            5. 回答要专业、严谨。
            
            请用中文回答。
            """;

    @Override
    public AIChatResponseVO chat(Long userId, AIChatRequestDTO request) {
        log.info("接收到AI对话请求: userId={}, role={}, message={}", 
                userId, request.getRole(), request.getMessage());

        if (!StringUtils.hasText(request.getMessage())) {
            return AIChatResponseVO.builder().reply("有什么我可以帮您的吗？").build();
        }

        String systemPrompt = STUDENT_SYSTEM_PROMPT;
        if (request.getRole() != null && request.getRole() == 2) {
            log.info("切换到教师模式对话: userId={}", userId);
            systemPrompt = TEACHER_SYSTEM_PROMPT;
        } else {
            log.info("使用默认学生模式对话: userId={}", userId);
        }

        try {
            String reply = deepSeekClient.chat(systemPrompt, request.getMessage());
            
            if (!StringUtils.hasText(reply)) {
                return AIChatResponseVO.builder().reply("抱歉，我暂时无法回答这个问题，请稍后再试。").build();
            }
            
            return AIChatResponseVO.builder().reply(reply).build();
            
        } catch (Exception e) {
            log.error("AI助手对话失败: userId={}, request={}", userId, request, e);
            return AIChatResponseVO.builder().reply("AI服务繁忙，请稍后再试。").build();
        }
    }
}
