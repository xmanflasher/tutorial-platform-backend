package com.waterballsa.tutorial_platform.service.condition;

import com.waterballsa.tutorial_platform.dto.condition.GymChallengeCondition;
import com.waterballsa.tutorial_platform.dto.condition.MissionConditionDto;
import com.waterballsa.tutorial_platform.entity.GymSubmission;
import com.waterballsa.tutorial_platform.repository.GymSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GymChallengeChecker implements ConditionChecker {

    private final GymSubmissionRepository gymRepo;

    @Override
    public boolean supports(String type) {
        return "GYM_CHALLENGE_SUCCESS".equals(type) || 
               "GYM_CHALLENGE_SUCCESS_PRACTICAL".equals(type);
    }

    @Override
    public boolean check(MissionConditionDto dto, Long userId) {
        if (!(dto instanceof GymChallengeCondition condition)) {
            return false;
        }

        Long targetGymId = condition.getTargetGymId();
        if (targetGymId == null) {
            return false; // 如果沒解析出 ID，視為未達成
        }

        // 檢查是否存在狀態為 SUCCESS 的繳交紀錄
        return gymRepo.existsByMemberIdAndGymIdAndStatus(userId, targetGymId, GymSubmission.SubmissionStatus.SUCCESS);
    }
}