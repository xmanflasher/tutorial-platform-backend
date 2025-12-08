package com.waterballsa.tutorial_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionDTO {
    private String id;
    private String name;
    private String description;
    private Integer durationInDays;
    private RewardDTO reward;
}