package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MemberMissionDTO {
    private Long missionId;
    private String name;
    private String description;
    private String status; // "AVAILABLE", "IN_PROGRESS", "COMPLETED", ...
    private String rewardDescription;
    private String unlockConditionDescription;
    private Integer duration;
    private LocalDateTime deadline;
    private Integer currentProgress;
    // ★ 新增：為了延長期限功能
    private int opportunityCardsUsed; // 已使用次數
    private int maxOpportunityCards;  // 最大次數 (通常是 2)
    private boolean isExtendable;     // 前端判斷按鈕是否可按 (cardsUsed < 2)
}