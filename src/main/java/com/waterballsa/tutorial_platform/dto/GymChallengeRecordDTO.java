package com.waterballsa.tutorial_platform.dto;

import lombok.Data;
import java.util.Map;

@Data
public class GymChallengeRecordDTO {
    private Long id;
    private Long userId;
    private Long journeyId;
    private Long chapterId;
    private Long gymId;
    private Long gymChallengeId;
    private Long createdAt; // 前端習慣收 Timestamp (long)
    private Map<String, String> ratings;
    private String feedback;
    private String status;
    private Long bookingCompletedAt;
    private Long completedAt;
    private Long reviewedAt;
    private Map<String, String> submission;
}