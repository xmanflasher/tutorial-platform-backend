package com.waterballsa.tutorial_platform.service.condition;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.waterballsa.tutorial_platform.dto.condition.MissionConditionDto;
import com.waterballsa.tutorial_platform.dto.condition.GymChallengeCondition;
import com.waterballsa.tutorial_platform.repository.GymSubmissionRepository; // 確保這個 Repo 存在

@Component
public class GymChallengeChecker implements ConditionChecker {

    @Autowired
    private GymSubmissionRepository gymRepo;

    @Override
    public boolean supports(String type) {
        return type.startsWith("GYM_CHALLENGE");
    }

    @Override
    public boolean check(MissionConditionDto dto, Long userId) {
        // 先回傳 false 讓編譯通過，之後再補上真實邏輯
        return false;
    }
}