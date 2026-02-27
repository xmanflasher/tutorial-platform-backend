package com.waterballsa.tutorial_platform.service.condition;

import com.waterballsa.tutorial_platform.dto.condition.MissionConditionDto;
import com.waterballsa.tutorial_platform.entity.MemberMission;
import com.waterballsa.tutorial_platform.repository.MemberMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MissionCompletedChecker implements ConditionChecker {

    private final MemberMissionRepository memberMissionRepo;

    @Override
    public boolean supports(String type) {
        return "MISSION_COMPLETED".equals(type);
    }

    @Override
    public boolean check(MissionConditionDto dto, Long userId) {
        // 使用解析後的 targetMissionId
        Long targetMissionId = dto.getTargetMissionId();
        if (targetMissionId == null) {
            return false;
        }

        Optional<MemberMission> record = memberMissionRepo.findByMember_IdAndMission_Id(userId, targetMissionId);
        
        return record.isPresent() && (
                record.get().getStatus() == MemberMission.MissionStatus.COMPLETED ||
                record.get().getStatus() == MemberMission.MissionStatus.CLAIMED
        );
    }
}
