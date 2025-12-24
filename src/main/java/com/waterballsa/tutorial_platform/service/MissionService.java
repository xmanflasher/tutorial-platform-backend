package com.waterballsa.tutorial_platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterballsa.tutorial_platform.dto.MemberMissionDTO;
import com.waterballsa.tutorial_platform.dto.condition.GymChallengeCondition;
import com.waterballsa.tutorial_platform.dto.condition.MissionCompletedCondition;
import com.waterballsa.tutorial_platform.dto.condition.MissionConditionDto;
import com.waterballsa.tutorial_platform.dto.condition.ExternalCondition;
import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.*;
import com.waterballsa.tutorial_platform.service.condition.ConditionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*; // 引入 HashMap 或是直接用 *
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepo;
    private final MemberMissionRepository memberMissionRepo;
    private final MemberRepository memberRepo;
    private final JourneyRepository journeyRepository;

    private final ConditionEvaluator conditionEvaluator;
    private final ObjectMapper objectMapper;

    // ★ 1. 讀取任務列表
    public List<MemberMissionDTO> getMissionsByJourneySlug(Long memberId, String journeySlug) {
        List<Mission> journeyMissions = missionRepo.findAllByJourney_Slug(journeySlug);

        if (journeyMissions.isEmpty()) {
            if (!journeyRepository.existsBySlug(journeySlug)) {
                return Collections.emptyList();
            }
        }

        List<Long> missionIds = journeyMissions.stream().map(Mission::getId).collect(Collectors.toList());
        List<MemberMission> myRecords = memberMissionRepo.findByMemberIdAndMissionIdIn(memberId, missionIds);

        List<MemberMissionDTO> result = new ArrayList<>();

        for (Mission mission : journeyMissions) {
            MemberMission record = myRecords.stream()
                    .filter(m -> m.getMission().getId().equals(mission.getId()))
                    .findFirst()
                    .orElse(null);

            String displayStatus;

            if (record != null) {
                displayStatus = record.getStatus().name();
            } else {
                boolean isUnlocked = checkRequirements(memberId, mission.getRequirements(), "PREREQUISITE");
                displayStatus = isUnlocked ? "AVAILABLE" : "LOCKED";
            }

            int usedCards = (record != null) ? record.getOpportunityCardsUsed() : 0;
            boolean isExtendable = (record != null)
                    && (record.getStatus() == MemberMission.MissionStatus.IN_PROGRESS)
                    && (usedCards < 2);

            String unlockDesc = formatRequirementDescription(mission.getRequirements(), "PREREQUISITE");

            result.add(MemberMissionDTO.builder()
                    .missionId(mission.getId())
                    .name(mission.getName())
                    .description(mission.getDescription())
                    .status(displayStatus)
                    .rewardDescription(formatReward(mission.getReward()))
                    .unlockConditionDescription(unlockDesc)
                    .duration(mission.getDurationInDays())
                    .deadline(record != null ? record.getDeadline() : null)
                    .currentProgress(record != null ? record.getCurrentProgress() : 0)
                    .opportunityCardsUsed(usedCards)
                    .maxOpportunityCards(2)
                    .isExtendable(isExtendable)
                    .build());
        }

        return result;
    }

    // ★ 2. 接受任務
    @Transactional
    public void acceptMission(Long memberId, Long missionId) {
        Mission mission = missionRepo.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

        boolean hasActiveMission = memberMissionRepo.existsByMemberIdAndStatus(memberId, MemberMission.MissionStatus.IN_PROGRESS);
        if (hasActiveMission) {
            throw new IllegalStateException("您已有進行中的任務，請先完成或放棄該任務。");
        }

        Optional<MemberMission> existingRecord = memberMissionRepo.findByMember_IdAndMission_Id(memberId, missionId);
        if (existingRecord.isPresent()) {
            throw new IllegalStateException("Mission already accepted or completed.");
        }

        if (!checkRequirements(memberId, mission.getRequirements(), "PREREQUISITE")) {
            throw new IllegalStateException("Mission is locked. Criteria not met.");
        }

        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        MemberMission newRecord = MemberMission.builder()
                .member(member)
                .mission(mission)
                .status(MemberMission.MissionStatus.IN_PROGRESS)
                .currentProgress(0)
                .opportunityCardsUsed(0)
                .build();

        if (mission.getDurationInDays() != null && mission.getDurationInDays() > 0) {
            newRecord.setDeadline(LocalDateTime.now().plusDays(mission.getDurationInDays()));
        }

        memberMissionRepo.save(newRecord);
    }

    // ★ 3. 延長期限
    @Transactional
    public void extendMissionDeadline(Long memberId, Long missionId) {
        MemberMission record = memberMissionRepo.findByMember_IdAndMission_Id(memberId, missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission record not found"));

        if (record.getStatus() != MemberMission.MissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("只能延長進行中的任務");
        }

        if (record.getOpportunityCardsUsed() >= 2) {
            throw new IllegalStateException("機會卡已用盡，無法再延長。");
        }

        record.setOpportunityCardsUsed(record.getOpportunityCardsUsed() + 1);
        if (record.getDeadline() != null && record.getDeadline().isAfter(LocalDateTime.now())) {
            record.setDeadline(record.getDeadline().plusDays(15));
        } else {
            record.setDeadline(LocalDateTime.now().plusDays(15));
        }

        memberMissionRepo.save(record);
    }

    // ★ 4. 領取獎勵
    @Transactional
    public void claimReward(Long memberId, Long missionId) {
        MemberMission record = memberMissionRepo.findByMember_IdAndMission_Id(memberId, missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission record not found"));

        if (record.getStatus() == MemberMission.MissionStatus.CLAIMED) {
            throw new IllegalStateException("Reward already claimed.");
        }
        if (record.getStatus() != MemberMission.MissionStatus.COMPLETED) {
            throw new IllegalStateException("Mission not completed yet.");
        }

        Mission mission = record.getMission();
        Member member = record.getMember();

        Reward reward = mission.getReward();
        if (reward != null) {
            if (reward.getExp() != null && reward.getExp() > 0) {
                member.setExp(member.getExp() + reward.getExp());
            }
            if (reward.getCoin() != null && reward.getCoin() > 0) {
                member.setCoin(member.getCoin() + reward.getCoin());
            }
            if (reward.getSubscriptionExtensionInDays() != null && reward.getSubscriptionExtensionInDays() > 0) {
                LocalDateTime currentExpiry = member.getSubscriptionEndDate();
                if (currentExpiry == null || currentExpiry.isBefore(LocalDateTime.now())) {
                    currentExpiry = LocalDateTime.now();
                }
                member.setSubscriptionEndDate(currentExpiry.plusDays(reward.getSubscriptionExtensionInDays()));
            }
        }

        record.setStatus(MemberMission.MissionStatus.CLAIMED);

        memberRepo.save(member);
        memberMissionRepo.save(record);
    }

    // --- Private Helpers ---

    private boolean checkRequirements(Long memberId, List<MissionRequirement> requirements, String category) {
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        return requirements.stream()
                .filter(req -> category.equals(req.getCategory()))
                .allMatch(req -> {
                    MissionConditionDto dto = mapToConditionDto(req);
                    return conditionEvaluator.isConditionMet(dto, memberId);
                });
    }

    /**
     * [Modified] 解決 Jackson 缺少 'type' 欄位導致轉換失敗的問題
     */
    private MissionConditionDto mapToConditionDto(MissionRequirement req) {
        try {
            // ★★★ 修正開始：手動建立 Map 並補上 "type" 欄位 ★★★
            Map<String, Object> params = new HashMap<>();

            // 1. 複製既有的 params (若資料庫存的是 null，就保持空 Map)
            if (req.getParams() != null) {
                params.putAll(req.getParams());
            }

            // 2. 將 DB 欄位 condition_type 的值，塞入 Map 的 "type" key
            // 這樣 Jackson 在做 convertValue 時，就能讀到 @JsonTypeInfo(property="type") 所需的資訊
            params.put("type", req.getConditionType());
            // ★★★ 修正結束 ★★★

            MissionConditionDto dto;

            // 這裡傳入的是補完 type 的 params Map
            switch (req.getConditionType()) {
                case "MISSION_COMPLETED":
                    dto = objectMapper.convertValue(params, MissionCompletedCondition.class);
                    break;
                case "GYM_CHALLENGE_SUCCESS":
                case "GYM_CHALLENGE_SUCCESS_PRACTICAL":
                    dto = objectMapper.convertValue(params, GymChallengeCondition.class);
                    break;
                case "EXTERNAL":
                    dto = new ExternalCondition();
                    break;
                default:
                    log.warn("Unknown condition type: {}", req.getConditionType());
                    dto = new ExternalCondition();
            }

            dto.setId(req.getId());
            dto.setName(req.getName());
            dto.setDescription(req.getDescription());
            dto.setType(req.getConditionType());
            dto.setRequiredQuantity(req.getRequiredQuantity());

            return dto;
        } catch (Exception e) {
            log.error("Failed to map requirement to DTO: {}", req, e);
            throw new RuntimeException("System Error: Condition mapping failed.");
        }
    }

    private String formatRequirementDescription(List<MissionRequirement> requirements, String category) {
        if (requirements == null || requirements.isEmpty()) return "無條件";

        return requirements.stream()
                .filter(req -> category.equals(req.getCategory()))
                .map(MissionRequirement::getName)
                .findFirst()
                .orElse("無條件");
    }

    private String formatReward(Reward reward) {
        if (reward == null) return "無獎勵";

        List<String> rewards = new ArrayList<>();

        // 1. 檢查經驗值
        if (reward.getExp() != null && reward.getExp() > 0) {
            rewards.add("經驗值 " + reward.getExp());
        }

        // 2. 檢查金幣 (reward_coin)
        if (reward.getCoin() != null && reward.getCoin() > 0) {
            rewards.add("金幣 " + reward.getCoin());
        }

        // 3. 檢查延長天數 (reward_sub_days)
        if (reward.getSubscriptionExtensionInDays() != null && reward.getSubscriptionExtensionInDays() > 0) {
            rewards.add("延長 " + reward.getSubscriptionExtensionInDays() + " 天");
        }

        // 4. 檢查外部獎勵說明 (external_reward_desc)
        if (reward.getExternalRewardDescription() != null && !reward.getExternalRewardDescription().isEmpty()) {
            rewards.add(reward.getExternalRewardDescription());
        }

        // 如果 DB 全部都是空的，回傳 "神秘獎勵" 或 "無"
        return rewards.isEmpty() ? "無" : String.join(", ", rewards);
    }
}