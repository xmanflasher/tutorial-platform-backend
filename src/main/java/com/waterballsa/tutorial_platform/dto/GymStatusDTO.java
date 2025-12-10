package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymStatusDTO {
    private Long gymId;
    private String name;
    private String status; // "LOCKED", "OPEN", "PASSED"
    private Integer stars;
}