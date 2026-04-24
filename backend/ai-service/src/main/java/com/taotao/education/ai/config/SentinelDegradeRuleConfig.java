package com.taotao.education.ai.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelDegradeRuleConfig {

    @PostConstruct
    public void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        rules.add(buildSlowCallRule("ai:deepseek:chat", 1200, 0.5d, 10, 10, 60));
        DegradeRuleManager.loadRules(rules);
    }

    private DegradeRule buildSlowCallRule(String resource,
                                          int maxRtMs,
                                          double slowRatioThreshold,
                                          int minRequestAmount,
                                          int statIntervalSeconds,
                                          int timeWindowSeconds) {
        DegradeRule rule = new DegradeRule(resource);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rule.setCount(maxRtMs);
        rule.setSlowRatioThreshold(slowRatioThreshold);
        rule.setMinRequestAmount(minRequestAmount);
        rule.setStatIntervalMs(statIntervalSeconds * 1000);
        rule.setTimeWindow(timeWindowSeconds);
        return rule;
    }
}
