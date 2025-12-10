package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MemberMissionDTO {
    private Long missionId;
    private String name;
    private String status; // "AVAILABLE", "IN_PROGRESS", "COMPLETED", ...
    private String rewardDescription;
    private String unlockConditionDescription;
    private LocalDateTime deadline;
}