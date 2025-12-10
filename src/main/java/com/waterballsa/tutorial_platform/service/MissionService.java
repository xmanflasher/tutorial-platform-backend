package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.MemberMissionDTO;
import com.waterballsa.tutorial_platform.entity.MemberMission;
import com.waterballsa.tutorial_platform.entity.Mission;
import com.waterballsa.tutorial_platform.repository.MemberMissionRepository;
import com.waterballsa.tutorial_platform.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepo;
    private final MemberMissionRepository memberMissionRepo;

    public List<MemberMissionDTO> getMissions(Long memberId) {
        List<Mission> allMissions = missionRepo.findAll();
        List<MemberMission> myMissions = memberMissionRepo.findByMemberId(memberId);

        List<MemberMissionDTO> result = new ArrayList<>();

        for (Mission mission : allMissions) {
            // 1. 檢查使用者是否有這任務的紀錄
            MemberMission record = myMissions.stream()
                    .filter(m -> m.getMission().getId().equals(mission.getId()))
                    .findFirst().orElse(null);

            String displayStatus = "LOCKED";

            if (record != null) {
                // 如果已經接了，直接用紀錄的狀態
                displayStatus = record.getStatus().name();
            } else {
                // 如果沒接過，檢查是否符合「開放條件」
                boolean unlocked = checkUnlockCondition(memberId, mission.getUnlockCondition());
                displayStatus = unlocked ? "AVAILABLE" : "LOCKED";
            }

            result.add(MemberMissionDTO.builder()
                    .missionId(mission.getId())
                    .name(mission.getName())
                    .status(displayStatus)
                    // 呼叫下方補上的 helper methods
                    .rewardDescription(formatReward(mission))
                    .unlockConditionDescription(formatCondition(mission.getUnlockCondition()))
                    .deadline(record != null ? record.getDeadline() : null)
                    .build());
        }
        return result;
    }

    // 接受任務
    @Transactional
    public void acceptMission(Long memberId, Long missionId) {
        Mission mission = missionRepo.findById(missionId).orElseThrow();
        // ... 建立 MemberMission 邏輯 ...
    }

    // 領取獎勵
    @Transactional
    public void claimReward(Long memberId, Long missionId) {
        // ... 領取獎勵邏輯 ...
    }

    // Helper: 解析條件字串 (例如 "gym_pass:3")
    private boolean checkUnlockCondition(Long memberId, String condition) {
        // 實作邏輯：去查 GymSubmission 表看 member 是否過了 gym #3
        return true; // 暫時回傳 true 方便測試
    }

    // ★★★ 補上這兩個缺失的方法 ★★★

    private String formatReward(Mission mission) {
        if (mission.getRewardType() == null) return "無獎勵";

        switch (mission.getRewardType()) {
            case SUBSCRIPTION:
                return "延長期限 " + mission.getRewardValue() + " 天";
            case ITEM:
                return "獲得道具 x" + mission.getRewardValue();
            case EXP:
                return "經驗值 " + mission.getRewardValue();
            case COIN:
                return "金幣 " + mission.getRewardValue();
            default:
                return mission.getRewardType().name();
        }
    }

    private String formatCondition(String condition) {
        if (condition == null || condition.isEmpty()) return "無條件";

        // 簡單解析範例：把 "gym_pass:3" 轉成中文
        if (condition.startsWith("gym_pass:")) {
            return "通過道館 " + condition.split(":")[1];
        }
        return condition;
    }
}