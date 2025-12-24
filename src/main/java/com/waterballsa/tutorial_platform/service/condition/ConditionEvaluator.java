package com.waterballsa.tutorial_platform.service.condition;

import com.waterballsa.tutorial_platform.dto.condition.MissionConditionDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConditionEvaluator {

    private final List<ConditionChecker> checkers;

    public ConditionEvaluator(List<ConditionChecker> checkers) {
        this.checkers = checkers;
    }

    public boolean isConditionMet(MissionConditionDto condition, Long userId) {
        return checkers.stream()
                .filter(checker -> checker.supports(condition.getType()))
                .findFirst()
                .map(checker -> checker.check(condition, userId))
                .orElse(true); // 如果找不到 Checker (例如 External)，預設回傳 true 或 false 視你的需求
    }
}