package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ChallengeRecordDTO {
    private Long id;
    private Long userId;
    private Long gymId;
    private Long gymChallengeId;

    // 這些是為了前端顯示方便，也可以只傳 ID 讓前端查
    private Long journeyId;
    private Long chapterId;

    private Long createdAt;          // 對應 submittedAt
    private Long bookingCompletedAt;
    private Long completedAt;        // 也是對應 submittedAt (API 命名慣例)
    private Long reviewedAt;

    private String status;           // SUCCESS, FAILED, SUBMITTED
    private String feedback;

    // 直接使用 Map，Converter 轉出來的結果會直接塞進這裡
    private Map<String, String> ratings;
    private Map<String, String> submission;
}