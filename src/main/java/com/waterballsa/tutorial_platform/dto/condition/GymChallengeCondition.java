package com.waterballsa.tutorial_platform.dto.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GymChallengeCondition extends MissionConditionDto {
    // 這裡只需要定義 JSON 中特有的欄位，父類別有的不用寫
    private Long journeyId;
    private Long chapterId;
    private Long gymId;
}