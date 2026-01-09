package com.waterballsa.tutorial_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor // ★ 建議補上：Jackson 反序列化時常需要空建構子
@AllArgsConstructor // ★ 建議補上：配合 Builder 使用
public class MemberMissionDTO {
    private Long missionId;
    private String name;
    private String description;
    private String status;
    private String rewardDescription;
    private String unlockConditionDescription;
    private Integer duration;
    private LocalDateTime deadline;
    private Integer currentProgress;
    private int opportunityCardsUsed;
    private int maxOpportunityCards;
    private boolean isExtendable;
}