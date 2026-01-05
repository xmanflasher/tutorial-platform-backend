package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymBadgeDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Long gymId;
    private Long journeyId;
    private Long chapterId;
    private boolean unlocked;
}