package com.waterballsa.tutorial_platform.dto.condition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        // 1. 任務完成
        @JsonSubTypes.Type(value = MissionCompletedCondition.class, name = "MISSION_COMPLETED"),

        // 2. 道館挑戰 (一般) - 使用 GymChallengeCondition
        @JsonSubTypes.Type(value = GymChallengeCondition.class, name = "GYM_CHALLENGE_SUCCESS"),

        // 3. 道館挑戰 (實戰) - 參數結構一樣，所以共用 GymChallengeCondition 類別
        @JsonSubTypes.Type(value = GymChallengeCondition.class, name = "GYM_CHALLENGE_SUCCESS_PRACTICAL"),

        // 4. 外部任務 - 參數很少，可能需要一個新的簡單類別，或是直接用基礎類別
        @JsonSubTypes.Type(value = ExternalCondition.class, name = "EXTERNAL")
})
public abstract class MissionConditionDto {
    private Long id;
    private String name;
    private String description;
    private String type; // 必須有這個欄位讓 Jackson 辨識
    private Integer requiredQuantity;
}