package com.waterballsa.tutorial_platform.dto.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MissionCompletedCondition extends MissionConditionDto {
    // 對應 JSON 中的 "missionId": 7
    private Long missionId;
}