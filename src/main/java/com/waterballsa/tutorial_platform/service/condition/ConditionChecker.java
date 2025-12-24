package com.waterballsa.tutorial_platform.service.condition;

import com.waterballsa.tutorial_platform.dto.condition.MissionConditionDto;

public interface ConditionChecker {
    // 告訴系統這個 Checker 負責哪種 type
    boolean supports(String type);

    // 執行檢查邏輯
    boolean check(MissionConditionDto condition, Long userId);
}