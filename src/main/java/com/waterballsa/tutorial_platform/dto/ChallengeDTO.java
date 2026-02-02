package com.waterballsa.tutorial_platform.dto;

import com.waterballsa.tutorial_platform.enums.ChallengeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDTO {
    private Long id;
    private String name;

    // 對應前端: "PRACTICAL_CHALLENGE" | "INSTANT_CHALLENGE"
    private ChallengeType type;

    private Integer recommendDurationInDays;
    private Integer maxDurationInDays;

    // 如果有需要提交欄位結構，可以之後再擴充
    // private List<SubmissionFieldDTO> submissionFields;
}